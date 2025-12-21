package p3project.classes;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class SponsorTest{

    @Test
    public void testSponsorCreation(){

        String sponsorName = "sponsorName";
        String contactPerson = "contactPerson";
        String email = "email";
        String phoneNumber = "12345678";
        String cvrNumber = "12345678";
        boolean status = true;
        String comments = "null";

        Sponsor sponsor = new Sponsor();
        sponsor.setName(sponsorName);
        sponsor.setContactPerson(contactPerson);
        sponsor.setEmail(email);
        sponsor.setPhoneNumber(phoneNumber);
        sponsor.setCvrNumber(cvrNumber);
        sponsor.setActive(status);
        sponsor.setComments(comments);

        assertEquals(sponsorName, sponsor.getName());
        assertEquals(contactPerson, sponsor.getContactPerson());
        assertEquals(email, sponsor.getEmail());
        assertEquals(phoneNumber, sponsor.getPhoneNumber());
        assertEquals(cvrNumber, sponsor.getCvrNumber());
        assertEquals(status, sponsor.getActive());
        assertEquals(comments, sponsor.getComments());
    }

    @Test
    public void testSponsorWithNullValues(){

        Sponsor sponsor = new Sponsor();

        assertNull(sponsor.getName());
        assertNull(sponsor.getContactPerson());
        assertNull(sponsor.getEmail());
        assertNull(sponsor.getPhoneNumber());
        assertNull(sponsor.getCvrNumber());
        assertFalse(sponsor.getActive()); // Boolean default = false
        assertNull(sponsor.getComments());
    }

}