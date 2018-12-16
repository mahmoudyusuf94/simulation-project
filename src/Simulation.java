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

    private static EventComparator comparator = new EventComparator();

    private static PriorityQueue<Event> arrivingJobs = new PriorityQueue<>(10, comparator);

    private static PriorityQueue<Event> firstClassWaitingJobs = new PriorityQueue<>(10, comparator);
    private static PriorityQueue<Event> secondClassWaitingJobs = new PriorityQueue<>(10, comparator);
    private static PriorityQueue<Event> thirdClassWaitingJobs = new PriorityQueue<>(10, comparator);

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
            arrivingJobs.add(event);
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
            arrivingJobs.add(event);
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
            arrivingJobs.add(event);
        }
    }

    private static void jobsLoop(){
        while(currentTime < SIMULATION_TIME){
//            System.out.println(currentTime);
            if(firstClassWaitingJobs.size()> 0 || secondClassWaitingJobs.size() >0 || thirdClassWaitingJobs.size() > 0){
                boolean queue1Changed = false;
                boolean queue2Changed = false;
                boolean queue3Changed = false;

                int taken = 0;
                double jobTime = 0;
                while(firstClassWaitingJobs.size() > 0 && taken < 4){
                    Event task = firstClassWaitingJobs.poll();
                    if(jobTime < task.getServingTime()) jobTime = task.getServingTime();
                    task.setWaitingTime(currentTime - task.getArrivalTime());
                    taken ++;
                    finishedJobs.add(task);
                    queue1Changed = true;
                }
                while(secondClassWaitingJobs.size() > 0 && taken < 4 ){
                    Event task = secondClassWaitingJobs.poll();
                    task.setWaitingTime(currentTime - task.getArrivalTime());
                    if(jobTime < task.getServingTime()) jobTime = task.getServingTime();
                    taken ++;
                    finishedJobs.add(task);
                }
                while(thirdClassWaitingJobs.size() > 0 && taken < 4){
                    Event task = thirdClassWaitingJobs.poll();
                    task.setWaitingTime(currentTime - task.getArrivalTime());
                    if(jobTime < task.getServingTime()) jobTime = task.getServingTime();
                    taken ++;
                    finishedJobs.add(task);
                }
                if(queue1Changed){
                    avgQueue1Len +=  firstClassWaitingJobs.size() * (currentTime - queue1LastChangeTime);
                    queue1LastChangeTime = currentTime;
                }
                if(queue2Changed){
                    avgQueue1Len +=  secondClassWaitingJobs.size() * (currentTime - queue1LastChangeTime);
                    queue2LastChangeTime = currentTime;
                }
                if(queue3Changed){
                    avgQueue3Len += thirdClassWaitingJobs.size() * (currentTime - queue3LastChangeTime);
                    queue3LastChangeTime = currentTime;
                }
                currentTime += jobTime;
            } else{
                Event task = arrivingJobs.poll();
                finishedJobs.add(task);
                currentTime = task.getArrivalTime() + task.getServingTime();
            }
            setWaitingTime();
        }
    }

    private static void setWaitingTime() {
        for (int i = 0 ; i < arrivingJobs.size(); i++){
            if (arrivingJobs.peek().getArrivalTime() < currentTime) {
                Event task = arrivingJobs.poll();
                switch (task.getEventClass()) {
                    case FIRST_CLASS_TYPE:
                        avgQueue1Len += firstClassWaitingJobs.size() * (task.getArrivalTime() - queue1LastChangeTime);
                        queue1LastChangeTime = task.getArrivalTime();
                        firstClassWaitingJobs.add(task);
                        break;
                    case SECOND_CLASS_TYPE:
                        avgQueue2Len += secondClassWaitingJobs.size() * (task.getArrivalTime() - queue2LastChangeTime);
                        queue2LastChangeTime = task.getArrivalTime();
                        secondClassWaitingJobs.add(task);
                        break;
                    case THIRD_CLASS_TYPE:
                        avgQueue3Len += thirdClassWaitingJobs.size() * (task.getArrivalTime() - queue3LastChangeTime);
                        queue3LastChangeTime = task.getArrivalTime();
                        thirdClassWaitingJobs.add(task);
                        break;
                }
            }
        }
    }

    public static void main(String[] args) {
        prepareSimulationJobs();
        jobsLoop();
        System.out.println("AVG QUEUE 1 length: "+ avgQueue1Len/SIMULATION_TIME);
        System.out.println("AVG QUEUE 2 length: "+ avgQueue2Len/SIMULATION_TIME);
        System.out.println("AVG QUEUE 3 length: "+ avgQueue3Len/SIMULATION_TIME);
        System.out.println("AVG QUEUE 1 length not not: "+ avgQueue1Len);

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

