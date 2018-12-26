import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

public class Simulation {

    private static final int FIFO_POLICY = 1;
    private static final int SJF_POLICY = 2;

    private static final int SIMULATION_TIME = 720 * 60;
    private static final int WARMUP_PERIOD = 30 * 60;

    private static final int NUMBER_OF_TASKS_PER_JOB = 4;
    private static final int QUEUE_PRIORITY_POLICY = FIFO_POLICY;

    private static final int FIRST_CLASS_TYPE = 1;
    private static final int SECOND_CLASS_TYPE = 2;
    private static final int THIRD_CLASS_TYPE = 3;

    private static final double FIRST_CLASS_INTERARRIVAL_MEAN  = 0.2;
    private static final double SECOND_CLASS_INTERARRIVAL_MEAN  = 1.6;
    private static final double THIRD_CLASS_INTERARRIVAL_MEAN  = 5.4;

    private static final double FIRST_CLASS_MIN_REQ = 0.05;
    private static final double SECOND_CLASS_MIN_REQ = 0.94;
    private static final double THIRD_CLASS_MIN_REQ = 4.00;

    private static final double FIRST_CLASS_MAX_REQ = 0.11;
    private static final double SECOND_CLASS_MAX_REQ = 1.83;
    private static final double THIRD_CLASS_MAX_REQ = 8.00;

    private static double currentTime = 0.0;

    private static double queue1LastChangeTime = 0.0;
    private static double queue2LastChangeTime = 0.0;
    private static double queue3LastChangeTime = 0.0;

    private static List<Integer> q1LenOverTime = new ArrayList<Integer>();
    private static List<Integer> q2LenOverTime = new ArrayList<Integer>();
    private static List<Integer> q3LenOverTime = new ArrayList<Integer>();

    private static double avgQueue1Len = 0.0;
    private static double avgQueue2Len = 0.0;
    private static double avgQueue3Len = 0.0;

    private static double avgTasksOnCPU = 0.0;
    private static int lastJobNumOfTasks = 0;
    private static double lastJobNumOfTasksChangeTime = 0.0;

    private static double cpuActiveTime = 0 ;

    private static int q1MaxLen = 0;
    private static int q1MinLen = Integer.MAX_VALUE;

    private static int q2MaxLen = 0;
    private static int q2MinLen = Integer.MAX_VALUE;

    private static int q3MaxLen = 0;
    private static int q3MinLen = Integer.MAX_VALUE;

    private static FifoComparator fifoComparator = new FifoComparator();
    private static SjfComparator sjfComparator = new SjfComparator();

    private static PriorityQueue<Event> arrivingJobs = new PriorityQueue<>(10, fifoComparator);

    private static PriorityQueue<Event> firstClassWaitingJobs;
    private static PriorityQueue<Event> secondClassWaitingJobs;
    private static PriorityQueue<Event> thirdClassWaitingJobs;

    private static PriorityQueue<Event> finishedJobs = new PriorityQueue<>(10, fifoComparator);

    private static void prepareSimulationJobs(){
        prepareFirstClassSimulationJobs();
        prepareSecondClassSimulation();
        prepareThirdClassSimulation();
    }

    private static void prepareFirstClassSimulationJobs(){
        double time = 0.0;
        double interarrivalTime;
        double servingTime;
        RandomUtils random = new RandomUtils();
        while (time < SIMULATION_TIME){
            interarrivalTime = random.getExponential(FIRST_CLASS_INTERARRIVAL_MEAN);
            servingTime = random.getRandomUniformInRange(FIRST_CLASS_MIN_REQ, FIRST_CLASS_MAX_REQ);
            time += interarrivalTime;
            Event event = new Event(FIRST_CLASS_TYPE, servingTime, time);
            arrivingJobs.add(event);
        }
    }

    private static void prepareSecondClassSimulation(){
        double time = 0.0;
        double interarrivalTime;
        double servingTime;
        RandomUtils random = new RandomUtils();
        while(time < SIMULATION_TIME){
            interarrivalTime = random.getExponential(SECOND_CLASS_INTERARRIVAL_MEAN);
            servingTime = random.getRandomUniformInRange(SECOND_CLASS_MIN_REQ, SECOND_CLASS_MAX_REQ);
            time += interarrivalTime;
            Event event = new Event(SECOND_CLASS_TYPE, servingTime, time);
            arrivingJobs.add(event);
        }
    }

    private static void prepareThirdClassSimulation(){
        double time = 0.0;
        double interarrivalTime;
        double servingTime;
        RandomUtils random = new RandomUtils();
        while(time < SIMULATION_TIME){
            interarrivalTime = random.getExponential(THIRD_CLASS_INTERARRIVAL_MEAN);
            servingTime = random.getRandomUniformInRange(THIRD_CLASS_MIN_REQ, THIRD_CLASS_MAX_REQ);
            time += interarrivalTime;
            Event event = new Event(THIRD_CLASS_TYPE, servingTime, time);
            arrivingJobs.add(event);
        }
    }

    private static void jobsLoop(){
        while(currentTime < SIMULATION_TIME){
            if(firstClassWaitingJobs.size()> 0 || secondClassWaitingJobs.size() >0 || thirdClassWaitingJobs.size() > 0){

                int queue1Changed = 0;
                int queue2Changed = 0;
                int queue3Changed = 0;

                int taken = 0;
                double jobTime = 0;

                while(firstClassWaitingJobs.size() > 0 && taken < NUMBER_OF_TASKS_PER_JOB){
                    Event task = firstClassWaitingJobs.poll();
                    if(jobTime < task.getServingTime()) jobTime = task.getServingTime();
                    task.setWaitingTime(currentTime - task.getArrivalTime());
                    taken ++;
                    finishedJobs.add(task);
                    queue1Changed ++;
                }

                while(secondClassWaitingJobs.size() > 0 && taken < NUMBER_OF_TASKS_PER_JOB){
                    Event task = secondClassWaitingJobs.poll();
                    task.setWaitingTime(currentTime - task.getArrivalTime());
                    if(jobTime < task.getServingTime()) jobTime = task.getServingTime();
                    taken ++;
                    finishedJobs.add(task);
                    queue2Changed ++;
                }

                while(thirdClassWaitingJobs.size() > 0 && taken < NUMBER_OF_TASKS_PER_JOB){
                    Event task = thirdClassWaitingJobs.poll();
                    task.setWaitingTime(currentTime - task.getArrivalTime());
                    if(jobTime < task.getServingTime()) jobTime = task.getServingTime();
                    taken ++;
                    finishedJobs.add(task);
                    queue3Changed ++;
                }

                if(queue1Changed > 0){
                    if(currentTime > WARMUP_PERIOD) {
                        avgQueue1Len +=  (firstClassWaitingJobs.size() + queue1Changed) * (currentTime - queue1LastChangeTime);
                    }
                    queue1LastChangeTime = currentTime;
                    q1LenOverTime.add(firstClassWaitingJobs.size());
                }

                if(queue2Changed > 0){
                    if(currentTime > WARMUP_PERIOD){
                        avgQueue1Len +=  (secondClassWaitingJobs.size() + queue2Changed) * (currentTime - queue2LastChangeTime);
                    }
                    queue2LastChangeTime = currentTime;
                    q2LenOverTime.add(secondClassWaitingJobs.size());
                }

                if(queue3Changed > 0){
                    if(currentTime > WARMUP_PERIOD) {
                        avgQueue3Len += (thirdClassWaitingJobs.size() + queue3Changed) * (currentTime - queue3LastChangeTime);
                    }
                    queue3LastChangeTime = currentTime;
                    q3LenOverTime.add(thirdClassWaitingJobs.size());
                }

                avgTasksOnCPU += lastJobNumOfTasks * (currentTime - lastJobNumOfTasksChangeTime) ;
                lastJobNumOfTasks = taken;
                lastJobNumOfTasksChangeTime = currentTime;

                currentTime += jobTime;
                cpuActiveTime += jobTime;

            } else if( arrivingJobs.peek().getArrivalTime() <= currentTime ){

                Event task = arrivingJobs.poll();
                finishedJobs.add(task);

                avgTasksOnCPU += lastJobNumOfTasks * (currentTime - lastJobNumOfTasksChangeTime) ;
                lastJobNumOfTasks = 1;
                lastJobNumOfTasksChangeTime = currentTime;

                currentTime = task.getArrivalTime() + task.getServingTime();
                cpuActiveTime += task.getServingTime();
            }
            else{

                avgTasksOnCPU += lastJobNumOfTasks * (currentTime - lastJobNumOfTasksChangeTime) ;
                lastJobNumOfTasks = 0;
                lastJobNumOfTasksChangeTime = currentTime;

                currentTime = arrivingJobs.peek().getArrivalTime();
            }
            setWaitingTime();
        }
    }


    private static void setWaitingTime() {
        for (int i = 0 ; i < arrivingJobs.size(); i++ ){
            if (arrivingJobs.peek().getArrivalTime() < currentTime) {
                Event task = arrivingJobs.poll();
                switch (task.getEventClass()) {
                    case FIRST_CLASS_TYPE:
                        if(currentTime > WARMUP_PERIOD){
                            avgQueue1Len += firstClassWaitingJobs.size() * (task.getArrivalTime() - queue1LastChangeTime);
                        }
                        queue1LastChangeTime = task.getArrivalTime();
                        firstClassWaitingJobs.add(task);
                        break;
                    case SECOND_CLASS_TYPE:
                        if(currentTime > WARMUP_PERIOD){
                            avgQueue2Len += secondClassWaitingJobs.size() * (task.getArrivalTime() - queue2LastChangeTime);
                        }
                        queue2LastChangeTime = task.getArrivalTime();
                        secondClassWaitingJobs.add(task);
                        break;
                    case THIRD_CLASS_TYPE:
                        if(currentTime > WARMUP_PERIOD){
                            avgQueue3Len += thirdClassWaitingJobs.size() * (task.getArrivalTime() - queue3LastChangeTime);
                        }
                        queue3LastChangeTime = task.getArrivalTime();
                        thirdClassWaitingJobs.add(task);
                        break;
                }
            }else{
                break;
            }
        }
        setQueuesLengths();
    }

    private static void setQueuesLengths() {

        q1LenOverTime.add(firstClassWaitingJobs.size());
        q2LenOverTime.add(secondClassWaitingJobs.size());
        q3LenOverTime.add(thirdClassWaitingJobs.size());

        if( currentTime > WARMUP_PERIOD ){
            if(firstClassWaitingJobs.size() > q1MaxLen) q1MaxLen = firstClassWaitingJobs.size();
            if(firstClassWaitingJobs.size() < q1MinLen) q1MinLen = firstClassWaitingJobs.size();

            if(secondClassWaitingJobs.size() > q2MaxLen) q2MaxLen = secondClassWaitingJobs.size();
            if(secondClassWaitingJobs.size() < q2MinLen) q2MinLen = secondClassWaitingJobs.size();

            if(thirdClassWaitingJobs.size() > q3MaxLen) q3MaxLen = thirdClassWaitingJobs.size();
            if(thirdClassWaitingJobs.size() < q3MinLen) q3MinLen = thirdClassWaitingJobs.size();
        }
    }

    public static void main(String[] args) throws IOException {
        switch (QUEUE_PRIORITY_POLICY){
            case FIFO_POLICY:
                firstClassWaitingJobs = new PriorityQueue<>(10, fifoComparator);
                secondClassWaitingJobs = new PriorityQueue<>(10, fifoComparator);
                thirdClassWaitingJobs = new PriorityQueue<>(10, fifoComparator);
                break;
            case SJF_POLICY:
                firstClassWaitingJobs = new PriorityQueue<>(10, sjfComparator);
                secondClassWaitingJobs = new PriorityQueue<>(10, sjfComparator);
                thirdClassWaitingJobs = new PriorityQueue<>(10, sjfComparator);
                break;
        }
            prepareSimulationJobs();
            jobsLoop();
            printResults();

    }

    private static void printResults() throws IOException {

        StringBuilder sb = new StringBuilder();

        sb.append("CPU UTILIZATION: "+ cpuActiveTime/SIMULATION_TIME + "\n");

        sb.append("Queue 1 Min Length: "+ q1MinLen + "\n");
        sb.append("Queue 1 Max Length: "+ q1MaxLen + "\n");

        sb.append("Queue 2 Min Length: "+ q2MinLen + "\n");
        sb.append("Queue 2 Max Length: "+ q2MaxLen + "\n");

        sb.append("Queue 3 Min Length: "+ q3MinLen + "\n");
        sb.append("Queue 3 Max Length: "+ q3MaxLen + "\n");

        sb.append("AVG QUEUE 1 length: "+ avgQueue1Len/SIMULATION_TIME + "\n");
        sb.append("AVG QUEUE 2 length: "+ avgQueue2Len/SIMULATION_TIME + "\n");
        sb.append("AVG QUEUE 3 length: "+ avgQueue3Len/SIMULATION_TIME + "\n");

        sb.append("AVG NUM OF TASKS ON THE CPU: "+ avgTasksOnCPU/SIMULATION_TIME + "\n");

        double class1Count=0.0;
        double class2Count=0.0;
        double class3Count=0.0;
        double class1Wait=0.0;
        double class2Wait=0.0;
        double class3Wait=0.0;

        for(Event task: finishedJobs){
            if(task.getArrivalTime() > WARMUP_PERIOD){
                switch (task.getEventClass()){
                    case 1:
                        class1Wait += task.getWaitingTime();
                        class1Count++;
                        break;
                    case 2:
                        class2Wait += task.getWaitingTime();
                        class2Count++;
                        break;
                    case 3:
                        class3Wait += task.getWaitingTime();
                        class3Count ++;
                        break;
                }
            }
        }

        sb.append("AVG QUEUE 1 Waiting Time -> "+ class1Wait/class1Count + "\n");
        sb.append("AVG QUEUE 2 Waiting Time -> "+ class2Wait/class2Count + "\n");
        sb.append("AVG QUEUE 3 Waiting Time -> "+ class3Wait/class3Count + "\n");

        sb.append("--------------------------- END ------------------------------\n\n");

        System.out.println(sb.toString());
        FileWriter fw = new FileWriter("SimResults", true);
        fw.write(sb.toString());
        fw.close();

        FileWriter comparisonFileWriter;
        if (NUMBER_OF_TASKS_PER_JOB == 4){
            comparisonFileWriter = new FileWriter("X1i", true);
        }else{
            comparisonFileWriter = new FileWriter("X2i", true);
        }
        comparisonFileWriter.write(class1Wait/class1Count + ", " + class2Wait/class2Count + ", " + class3Wait/class3Count + "\n");
        comparisonFileWriter.close();

        FileWriter fw1 = new FileWriter("q1", true);
        for(int i = 0; i < q1LenOverTime.size()-1; i++){
            fw1.write(q1LenOverTime.get(i) + ", ");
        }
        fw1.write(q1LenOverTime.get(q1LenOverTime.size()-1)+"");
        fw1.write('\n');
        fw1.close();

        FileWriter fw2 = new FileWriter("q2", true);
        for(int i = 0; i < q2LenOverTime.size()-1; i++){
            fw2.write(q2LenOverTime.get(i) + ", ");
        }
        fw2.write(q2LenOverTime.get(q2LenOverTime.size()-1)+"");
        fw2.write('\n');
        fw2.close();

        FileWriter fw3 = new FileWriter("q3", true);
        for(int i = 0; i < q3LenOverTime.size()-1; i++){
            fw3.write(q3LenOverTime.get(i) + ", ");
        }
        fw3.write(q3LenOverTime.get(q3LenOverTime.size()-1)+"");
        fw3.write('\n');
        fw3.close();

    }

    static final class FifoComparator implements Comparator<Event>{
        @Override
        public int compare(Event o1, Event o2) {
            if(o1.getArrivalTime()< o2.getArrivalTime()){
                return  -1;
            }else{
                return 1;
            }
        }
    }

    static final class SjfComparator implements Comparator<Event>{
        @Override
        public int compare(Event o1, Event o2) {
            if(o1.getServingTime() < o2.getServingTime()){
                return  -1;
            }else{
                return 1;
            }
        }
    }


}

