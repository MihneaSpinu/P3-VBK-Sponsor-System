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
    private LocalDate startDate;
    private LocalDate endDate;
    private int payment;
    private boolean status;
    private String type;

    // Link contract to sponsor by sponsorName 
    private String sponsorName;

    @Lob
    @Column(name = "pdf_data", columnDefinition = "LONGBLOB") /*Longblob to have enough storage for .pdf's */
    private byte[] pdfData;
    
   
    // Constructor
    public Contract(LocalDate startDate, LocalDate endDate, int payment, boolean status, String typeName) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.payment = payment;
        this.status = status;
        this.type = typeName;
    }

    public Contract() {
    // required by JPA
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

    public byte[] getPdfData(){
        return this.pdfData;
    }

    public void setPdfData(byte[] pdfData){
        this.pdfData = pdfData;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public int getPayment() {
        return payment;
    }

    public void setPayment(int payment) {
        this.payment = payment;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }
    public String getType() {
        return type;
    }
    public void setType(String type)
    {
        this.type = type;
    }

    // Returns the name of the sponsor
    public String getSponsorName() {
        return sponsorName;
    }

    // Sets the name of the sponsor
    public void setSponsorName(String sponsorName) {
        this.sponsorName = sponsorName;
    }
    
    // Returns the ID of the entity
    public Long getId() {
        return id;
    }
}
