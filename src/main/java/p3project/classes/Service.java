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
    private String type;
    private boolean archived;
    private int amountOrDivision;
    private LocalDate startDate;
    private LocalDate endDate;

    // no-arg constructor required by JPA
    protected Service() {}

    // Constructor
    public Service(Long contractId, String name, String type, boolean archived, int amountOrDivision, LocalDate startDate, LocalDate endDate) {
        this.contractId = contractId;
        this.name = name;
        this.type = type;
        this.archived = archived;
        this.amountOrDivision = amountOrDivision;
        this.startDate = startDate;
        this.endDate = endDate;
    }


    public Long getId() { 
        return id; 
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getType() { 
        return type; 
    }
    public void setType(String type) { 
        this.type = type; 
    }

    public Service(String type, boolean archived, int amountOrDuration) {
        this.type = type;
        this.archived = archived;
        this.amountOrDivision = amountOrDuration;
    }

    public void setArchived(boolean archived) {
        this.archived = archived;
    }

    public boolean getArchived() {
        return this.archived;
    }

    public int getAmountOrDivision() { 
        return amountOrDivision; 
    }
    public void setAmountOrDivision(int amountOrDivision) { 
        this.amountOrDivision = amountOrDivision; 
    }

    public LocalDate getStartDate() { 
        return startDate; 
    }
    public void setStartDate(LocalDate startDate) { 
        this.startDate = startDate; 
    }

    public LocalDate getEndDate() { 
        return endDate; 
    }
    public void setEndDate(LocalDate endDate) { 
        this.endDate = endDate; 
    }

}
