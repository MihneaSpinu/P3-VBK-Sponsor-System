package p3project.classes;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;

@Entity
public class Contract {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long sponsorId;
    private LocalDate startDate;
    private LocalDate endDate;
    private String payment;
    private String type;
    private boolean active;
    private String name;

    @Lob
    @Column(columnDefinition = "LONGBLOB") /* Longblob to have enough storage for .pdf's */
    private byte[] pdfData;
    private String fileName;

    // Constructor
    public Contract(LocalDate startDate, LocalDate endDate, String payment, boolean active, String typeName) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.payment = payment;
        this.active = active;
        this.type = typeName;
    }

    public Contract() {
        // required by JPA
    }

    // Returns the ID of the entity
    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getSponsorId() {
        return sponsorId;
    }


    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }


    public void setSponsorId(Long sponsorId) {
        this.sponsorId = sponsorId;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        // Hvis endDate allerede er sat, sørg for at startDate ikke er efter den
        if (startDate != null && this.endDate != null && startDate.isAfter(this.endDate)) {
            throw new IllegalArgumentException("Contract start date cannot be after end date");
        }
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public byte[] getPdfData() {
        return this.pdfData;
    }

    public void setPdfData(byte[] pdfData) {
        this.pdfData = pdfData;
    }

    public void setEndDate(LocalDate endDate) {
        // Hvis startDate allerede er sat, sørg for at endDate ikke er før den
        if (endDate != null && this.startDate != null && endDate.isBefore(this.startDate)) {
            throw new IllegalArgumentException("Kontraktens slutdato kan ikke være før startdatoen");
        }
        this.endDate = endDate;
    }

    public String getPayment() {
        return payment;
    }


    public int getPaymentAsInt() {
        if (this.payment == null || this.payment.isEmpty()) return 0;
        try {
            return Integer.parseInt(this.payment);
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    public void setPayment(String payment) {
        this.payment = payment;
    }


    public void setPayment(int payment) {
        this.payment = String.valueOf(payment);
    }

    public boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


    public void setFileName(String fileName){
        this.fileName = fileName;
    }

    public String getFileName(){
        return this.fileName;
    }

}
