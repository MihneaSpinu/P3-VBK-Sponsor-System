package p3project.classes;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private ServiceType type;
    private boolean status;
    private int amountOrDuration;

    // Constructor
    public Service(ServiceType type, boolean status, int amountOrDuration) {
        this.type = type;
        this.status = status;
        this.amountOrDuration = amountOrDuration;
    }

    public Long getId() {
        return this.id;
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
