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
    private boolean active;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer amount;
    private Integer division;

    protected Service() {}

    public Service(Long contractId, String name, String type, boolean active, LocalDate startDate, LocalDate endDate, int amount, int division) {
        this.contractId = contractId;
        this.name = name;
        this.type = type;
        this.active = active;
        this.startDate = startDate;
        this.endDate = endDate;
        this.amount = amount;
        this.division = division;
    }


    public Long getId() { 
        return id; 
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getContractId() {
        return this.contractId; 
    }
    public void setContractId(Long contractId) { 
        this.contractId = contractId; 
    }

    public String getName() { 
        return this.name; 
    }
    public void setName(String name) { 
        this.name = name; 
    }

    public String getType() { 
        return this.type; 
    }
    public void setType(String type) { 
        this.type = type; 
    }

    public Service(String type, boolean active, int amountOrDuration) {
        this.type = type;
        this.active = active;
        this.amount = amountOrDuration;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean getActive() {
        return this.active;
    }

    public Integer getAmount() {
        return this.amount == null ? 0 : this.amount;
    }
    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getDivision() {
        return this.division == null ? 0 : this.division;
    }
    public void setDivision(Integer division) {
        this.division = division;
    }


    public LocalDate getStartDate() { 
        return this.startDate; 
    }
    public void setStartDate(LocalDate startDate) { 
        this.startDate = startDate; 
    }

    public LocalDate getEndDate() { 
        return this.endDate; 
    }
    public void setEndDate(LocalDate endDate) { 
        this.endDate = endDate; 
    }

}
