package p3project.classes;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class Sponsor {

    @Id
    private Long id;
    private String sponsorName;
    private String contactPerson;
    private String email;
    private String phoneNumber;
    private String cvrNumber;
    private boolean status;
    private String comments;

    // Constructor
    public Sponsor(String sponsorName, String contactPerson, String email,
                    String phoneNumber, String cvrNumber, boolean status, String comments) {
        this.sponsorName = sponsorName;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.cvrNumber = cvrNumber;
        this.status = status;
        this.comments = comments;
    }

    public Sponsor() {
    // Required by JPA
    }

    // Getters and Setters
    public Long getId() {
        return this.id;
    }

    public String getSponsorName() {
        return sponsorName;
    }

    public void setSponsorName(String sponsorName) {
        this.sponsorName = sponsorName;
    }

    public String getContactPerson() {
        return contactPerson;
    }

    public void setContactPerson(String contactPerson) {
        this.contactPerson = contactPerson;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getCvrNumber() {
        return cvrNumber;
    }

    public void setCvrNumber(String cvrNumber) {
        this.cvrNumber = cvrNumber;
    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
