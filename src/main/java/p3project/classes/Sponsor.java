package p3project.classes;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Sponsor {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String contactPerson;
    private String email;
    private String phoneNumber;
    private String cvrNumber;
    private boolean active;
    private String comments;

    // Constructor
    public Sponsor(String name, String contactPerson, String email,
                    String phoneNumber, String cvrNumber, boolean active, String comments) {
        this.name = name;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.cvrNumber = cvrNumber;
        this.active = active;
        this.comments = comments;
    }

    public Sponsor() {
    // Required by JPA
    }

    public Long getId() {
        return this.id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public void setActive(boolean active) {
        this.active = active;
    }

    public boolean getActive(){
        return active;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }
}
