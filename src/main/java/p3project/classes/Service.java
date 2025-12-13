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
    private Integer amount;
    private Integer division;
    private LocalDate startDate;
    private LocalDate endDate;

    // no-arg constructor required by JPA
    protected Service() {}

    // Constructor
    public Service(Long contractId, String name, String type, boolean active, int amountOrDivision, LocalDate startDate, LocalDate endDate) {
        this.contractId = contractId;
        this.name = name;
        this.type = type;
        this.active = active;
        this.amount = amountOrDivision;
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
        return amount == null ? 0 : amount;
    }
    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getDivision() {
        return division == null ? 0 : division;
    }
    public void setDivision(Integer division) {
        this.division = division;
    }

    // Backwards compatibility for code still using amountOrDivision
    public int getAmountOrDivision() {
        return getAmount();
    }
    public void setAmountOrDivision(int value) {
        this.amount = value;
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
