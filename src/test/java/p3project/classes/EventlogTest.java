package p3project.classes;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

public class EventlogTest {

    @Test
    public void constructor_withNamedTarget_setsAllReadableFields() {
        User user = new User();
        user.setName("TestUser");

        Sponsor sponsor = new Sponsor();
        sponsor.setName("Acme Corp");

        Eventlog log = new Eventlog(user, sponsor, "CREATE");

        assertEquals("TestUser", log.getUsername());
        assertEquals("Sponsor", log.getObjectType());
        assertEquals("Acme Corp", log.getObjectName());
        assertEquals("CREATE", log.getAction());

        String ts = log.getTimestamp();
        assertNotNull(ts);
        assertTrue(
            Pattern.matches("^\\d{2}/\\d{2}-\\d{4}\\s{2}\\d{2}:\\d{2}$", ts),
            "Timestamp should match pattern dd/MM-yyyy  HH:mm, was: " + ts
        );
    }

    @Test
    public void constructor_withoutGetName_setsHyphenObjectName() {
        User user = new User();
        user.setName("Tester");

        Integer target = 42; // No getName() method

        Eventlog log = new Eventlog(user, target, "UPDATE");

        assertEquals("Tester", log.getUsername());
        assertEquals("Integer", log.getObjectType());
        assertEquals("-", log.getObjectName());
        assertEquals("UPDATE", log.getAction());
    }

    @Test
    public void constructor_withNullAction_allowsNullAction() {
        User user = new User();
        user.setName("Tester");

        Sponsor sponsor = new Sponsor();
        sponsor.setName("No Action");

        Eventlog log = new Eventlog(user, sponsor, null);
        assertNull(log.getAction());
    }

    @Test
    public void constructor_withNullUser_throwsNullPointerException() {
        Sponsor sponsor = new Sponsor();
        sponsor.setName("Target");

        assertThrows(NullPointerException.class, () -> new Eventlog(null, sponsor, "ACT"));
    }

    @Test
    public void constructor_withNullTarget_throwsNullPointerException() {
        User user = new User();
        user.setName("Tester");

        assertThrows(NullPointerException.class, () -> new Eventlog(user, null, "ACT"));
    }
}
