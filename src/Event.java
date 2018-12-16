public class Event {

    private double arrivalTime;
    private double waitingTime;
    private double servingTime;
    private int eventClass;
    private boolean isWaiting;

    public Event(int eventClass, double servingTime, double arrivalTime){
        this.eventClass = eventClass;
        this.servingTime = servingTime;
        this.arrivalTime = arrivalTime;
        this.isWaiting = false;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(double arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public double getWaitingTime() {
        return waitingTime;
    }

    public void setWaitingTime(double waitingTime) {
        this.waitingTime = waitingTime;
    }

    public double getServingTime() {
        return servingTime;
    }

    public void setServingTime(double servingTime) {
        this.servingTime = servingTime;
    }

    public int getEventClass() {
        return eventClass;
    }

    public void setEventClass(int eventClass) {
        this.eventClass = eventClass;
    }

    public boolean isWaiting() {
        return isWaiting;
    }

    public void setWaiting(boolean waiting) {
        isWaiting = waiting;
    }

}
