package p3project.classes;

import java.time.LocalDate;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "services")
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long contractId;
    private String name;
    private ServiceType type;
    private ServiceStatus status;
    private int amountOrDivision;
    private LocalDate startDate;
    private LocalDate endDate;

    // no-arg constructor required by JPA
    protected Service() {}

    // Constructor
    public Service(Long contractId, String name, ServiceType type, ServiceStatus status, int amountOrDivision, LocalDate startDate, LocalDate endDate) {
        this.contractId = contractId;
        this.name = name;
        this.type = type;
        this.status = status;
        this.amountOrDivision = amountOrDivision;
        this.startDate = startDate;
        this.endDate = endDate;
    }


    public Long getId() { 
        return id; 
    }

    public Long getContractId() {
        return contractId; 
    }
    public void setContractId(Long contractId) { 
        this.contractId = contractId; 
    }

    public String getName() { 
        return name; 
    }
    public void setName(String name) { 
        this.name = name; 
    }

    public ServiceType getType() { 
        return type; 
    }
    public void setType(ServiceType type) { this.type = type; }

    public Service(ServiceType type, boolean status, int amountOrDuration) {
        this.type = type;
        this.setStatus(status);
        this.amountOrDivision = amountOrDuration;
    }

    public boolean getStatus() { return this.status == ServiceStatus.AKTIV; }
    public void setStatus(boolean active) { this.status = active ? ServiceStatus.AKTIV : ServiceStatus.INAKTIV; }


    public ServiceStatus getStatusEnum() { return status; }
    public void setStatus(ServiceStatus status) { this.status = status; }

    public int getAmountOrDuration() { return amountOrDivision; }
    public void setAmountOrDuration(int amount) { this.amountOrDivision = amount; }

    public int getAmountOrDivision() { return amountOrDivision; }
    public void setAmountOrDivision(int amountOrDivision) { this.amountOrDivision = amountOrDivision; }

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }

    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }

    // Nested enum for service status (AKTIV, IGANG, UDFORT, INAKTIV)
    public static enum ServiceStatus {
        AKTIV,
        IGANG,
        UDFORT,
        INAKTIV
    }
}
