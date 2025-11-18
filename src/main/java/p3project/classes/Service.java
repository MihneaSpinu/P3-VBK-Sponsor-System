package p3project.classes;

public class Service {
    
    private ServiceType type;
    private boolean status;
    private int amountOrDuration;

    // Constructor
    public Service(ServiceType type, boolean status, int amountOrDuration) {
        this.type = type;
        this.status = status;
        this.amountOrDuration = amountOrDuration;
    }

    // Getters and Setters
    public ServiceType getType() {
        return type;
    }

    public void setType(ServiceType type) {
        this.type = type;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public int getAmountOrDuration() {
        return amountOrDuration;
    }

    public void setAmountOrDuration(int amountOrDuration) {
        this.amountOrDuration = amountOrDuration;
    }
}
