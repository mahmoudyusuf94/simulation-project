import java.util.ArrayList;
import java.util.Comparator;
import java.util.PriorityQueue;

public class Simulation {

    private static final double SIMULATION_TIME = 720 * 60;

    private static final int FIRST_CLASS_TYPE = 1;
    private static final int SECOND_CLASS_TYPE = 2;
    private static final int THIRD_CLASS_TYPE = 3;

    private static final double FIRST_CLASS_INTERARRIVAL_MEAN  = 0.2;
    private static final double SECOND_CLASS_INTERARRIVAL_MEAN  = 0.2;
    private static final double THIRD_CLASS_INTERARRIVAL_MEAN  = 0.2;

    private static final double FIRST_CLASS_MIN_REQ = 0.05;
    private static final double SECOND_CLASS_MIN_REQ = 0.94;
    private static final double THIRD_CLASS_MIN_REQ = 4.00;

    private static final double FIRST_CLASS_MAX_REQ = 0.11;
    private static final double SECOND_CLASS_MAX_REQ = 1.83;
    private static final double THIRD_CLASS_MAX_REQ = 8.00;

    private static double currentTime = 0.0;
    private static boolean isIdle;

    private static double queue1LastChangeTime = 0.0;
    private static double queue2LastChangeTime = 0.0;
    private static double queue3LastChangeTime = 0.0;

    private static double avgQueue1Len = 0.0;
    private static double avgQueue2Len = 0.0;
    private static double avgQueue3Len = 0.0;

    private static int queue1Len;
    private static int queue2Len;
    private static int queue3Len;

    private static int waitingInQ1;
    private static int waitingInQ2;
    private static int waitingInQ3;

    private static EventComparator comparator = new EventComparator();
    private static PriorityQueue<Event> firstClassJobs = new PriorityQueue<>(10, comparator);
    private static PriorityQueue<Event> secondClassJobs = new PriorityQueue<>(10, comparator);
    private static PriorityQueue<Event> thirdClassJobs = new PriorityQueue<>(10, comparator);

    private static PriorityQueue<Event> finishedJobs = new PriorityQueue<>(10, comparator);

    private static void prepareSimulationJobs(){
        prepareFirstClassSimulationJobs();
        prepareSecondClassSimulation();
        prepareThirdClassSimulation();
    }

    private static void prepareFirstClassSimulationJobs(){
        double time = 0.0;
        double interarrivalTime;
        double servingTime;
        while (time < SIMULATION_TIME){
            interarrivalTime = RandomUtils.getExponential(FIRST_CLASS_INTERARRIVAL_MEAN);
            servingTime = RandomUtils.getRandomUniformInRange(FIRST_CLASS_MIN_REQ, FIRST_CLASS_MAX_REQ);
            time += interarrivalTime;
            Event event = new Event(FIRST_CLASS_TYPE, servingTime, time);
            firstClassJobs.add(event);
        }
    }

    private static void prepareSecondClassSimulation(){
        double time = 0.0;
        double interarrivalTime;
        double servingTime;
        while(time < SIMULATION_TIME){
            interarrivalTime = RandomUtils.getExponential(SECOND_CLASS_INTERARRIVAL_MEAN);
            servingTime = RandomUtils.getRandomUniformInRange(SECOND_CLASS_MIN_REQ, SECOND_CLASS_MAX_REQ);
            time += interarrivalTime;
            Event event = new Event(SECOND_CLASS_TYPE, servingTime, time);
            secondClassJobs.add(event);
        }
    }

    private static void prepareThirdClassSimulation(){
        double time = 0.0;
        double interarrivalTime;
        double servingTime;
        while(time < SIMULATION_TIME){
            interarrivalTime = RandomUtils.getExponential(THIRD_CLASS_INTERARRIVAL_MEAN);
            servingTime = RandomUtils.getRandomUniformInRange(THIRD_CLASS_MIN_REQ, THIRD_CLASS_MAX_REQ);
            time += interarrivalTime;
            Event event = new Event(THIRD_CLASS_TYPE, servingTime, time);
            thirdClassJobs.add(event);
        }
    }

    private static void jobsLoop(){
        while(currentTime < SIMULATION_TIME){
//            System.out.println(currentTime);
            if(waitingInQ1 > 0 || waitingInQ2 > 0 || waitingInQ3 > 0){
                int taken = 0;
                double jobTime = 0;
                while(waitingInQ1 > 0 && taken < 4 && !firstClassJobs.isEmpty()){
                    Event task = firstClassJobs.poll();
                    if(jobTime < task.getServingTime()) jobTime = task.getServingTime();
                    taken ++;
                    finishedJobs.add(task);
//                    ///
                    avgQueue1Len += waitingInQ1 * (currentTime - queue1LastChangeTime);
                    queue1LastChangeTime = currentTime;
//                    ///
                    waitingInQ1 --;
                }
                while(waitingInQ2 > 0 && taken < 4 && !secondClassJobs.isEmpty()){
                    Event task = secondClassJobs.poll();
                    if(jobTime < task.getServingTime()) jobTime = task.getServingTime();
                    taken ++;
                    finishedJobs.add(task);
//                    ///
                    avgQueue2Len += waitingInQ2 * (currentTime - queue2LastChangeTime);
                    queue2LastChangeTime = currentTime;
//                    ///
                    waitingInQ2 --;
                }
                while(waitingInQ3 > 0 && taken < 4 && !thirdClassJobs.isEmpty()){
                    Event task = thirdClassJobs.poll();
                    if(jobTime < task.getServingTime()) jobTime = task.getServingTime();
                    taken ++;
                    finishedJobs.add(task);
//                    ///
                    avgQueue3Len -= waitingInQ3 * (currentTime - queue3LastChangeTime);
                    queue3LastChangeTime = currentTime;
//                    ///
                    waitingInQ3 --;
                }
                currentTime += jobTime;
            }else{
                double first = firstClassJobs.peek().getArrivalTime();
                double second = firstClassJobs.peek().getArrivalTime();
                double third = firstClassJobs.peek().getArrivalTime();
                if(first < second && first < third){
                    Event task = firstClassJobs.poll();
                    currentTime += task.getArrivalTime() + task.getServingTime();
                }else if( second < first && second < third){
                    Event task = secondClassJobs.poll();
                    currentTime += task.getArrivalTime() + task.getServingTime();
                }else{
                    Event task = thirdClassJobs.poll();
                    currentTime += task.getArrivalTime() + task.getServingTime();
                }
            }
            setWaitingTime();
        }
    }

    private static void setWaitingTime() {
            for (int i = 0 ; i < firstClassJobs.size(); i++){
//            for (Event task : firstClassJobs) {
                ArrayList<Event> tasks = new ArrayList<Event>();
                if (firstClassJobs.peek().getArrivalTime() < currentTime) {
                    Event task = firstClassJobs.poll();
                    task.setWaitingTime(currentTime - task.getArrivalTime());
                    if(!task.isWaiting()){
                        task.setWaiting(true);
                        avgQueue1Len += waitingInQ1 * (task.getArrivalTime() - queue1LastChangeTime);
                        queue1LastChangeTime = task.getArrivalTime();
                        waitingInQ1++;
//                        queue1Len++;
                    }
                    tasks.add(task);
                } else {
                    for(Event t: tasks){
                        firstClassJobs.add(t);
                    }
                    break;
                }
            }
        for (int i = 0 ; i < secondClassJobs.size(); i++){
//            for (Event task : secondClassJobs) {
            ArrayList<Event> tasks = new ArrayList<Event>();
            if (secondClassJobs.peek().getArrivalTime() < currentTime) {
                Event task = secondClassJobs.poll();
                task.setWaitingTime(currentTime - task.getArrivalTime());
                    if(!task.isWaiting()){
                        task.setWaiting(true);
                        avgQueue2Len += waitingInQ2 * (task.getArrivalTime() - queue2LastChangeTime);
                        queue2LastChangeTime = task.getArrivalTime();
                        waitingInQ2++;
//                        queue2Len++;
                    }
                    tasks.add(task);
                } else {
                    for(Event t: tasks) {
                        secondClassJobs.add(t);
                    }
                    break;
                }
            }
            for(int i =0 ; i < thirdClassJobs.size() ;i++){
                ArrayList<Event> tasks = new ArrayList<Event>();

//            for (Event task : thirdClassJobs) {
                if (thirdClassJobs.peek().getArrivalTime() < currentTime) {
                    Event task = thirdClassJobs.poll();
                    task.setWaitingTime(currentTime - task.getArrivalTime());
                    if(!task.isWaiting()){
                        task.setWaiting(true);
                        avgQueue3Len += waitingInQ3 * (task.getArrivalTime() - queue3LastChangeTime);
                        queue3LastChangeTime = task.getArrivalTime();
                        waitingInQ3++;
//                        queue3Len++;
                    }
                    tasks.add(task);
                } else {
                    for(Event t: tasks){
                        thirdClassJobs.add(t);
                    }
                    break;
                }
            }
    }

    public static void main(String[] args) {
        prepareSimulationJobs();
        jobsLoop();
        System.out.println("AVG QUEUE 1 length: "+ avgQueue1Len/SIMULATION_TIME);
        System.out.println("AVG QUEUE 2 length: "+ avgQueue2Len/SIMULATION_TIME);
        System.out.println("AVG QUEUE 3 length: "+ avgQueue3Len/SIMULATION_TIME);
        double class1Count=0.0;
        double class2Count=0.0;
        double class3Count=0.0;
        double class1Wait=0.0;
        double class2Wait=0.0;
        double class3Wait=0.0;
        for(Event task: finishedJobs){
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
        System.out.println("AVG QUEUE 1 Waiting -> "+ class1Wait/class1Count);
        System.out.println("AVG QUEUE 2 Waiting -> "+ class2Wait/class2Count);
        System.out.println("AVG QUEUE 3 Waiting -> "+ class3Wait/class3Count);
    }

    static final class EventComparator implements Comparator<Event>{
        @Override
        public int compare(Event o1, Event o2) {
            if(o1.getArrivalTime()< o2.getArrivalTime()){
                return  -1;
            }else{
                return 1;
            }
        }
    }
}

