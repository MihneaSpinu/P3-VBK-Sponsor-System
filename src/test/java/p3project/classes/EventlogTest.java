package p3project.classes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

public class EventlogTest {

    @Test
    public void testLogCreation() {
        User user = new User();
        Sponsor sponsor = new Sponsor();
        sponsor.setName("sponsorTestName");
        user.setName("TestName");
        // Eventlog log = Eventlog.create(user, sponsor, "testAction");
        // assertEquals(log.objectType, ("sponsorTestName")); 
    }
    
}
