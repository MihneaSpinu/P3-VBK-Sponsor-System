public class Sponsor {
    private String sponsorName;
    private String contactPerson;
    private String email;
    private int phoneNumber;
    private int cvrNumber;
    private boolean status;
    private String comments;

    // Constructor
    public Sponsor(String sponsorName, String contactPerson, String email,
                   int phoneNumber, int cvrNumber, boolean status, String comments) {
        this.sponsorName = sponsorName;
        this.contactPerson = contactPerson;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.cvrNumber = cvrNumber;
        this.status = status;
        this.comments = comments;
    }

    // Getters and Setters
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

    public int getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(int phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public int getCvrNumber() {
        return cvrNumber;
    }

    public void setCvrNumber(int cvrNumber) {
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
