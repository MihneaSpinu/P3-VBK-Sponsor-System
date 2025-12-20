package p3project.classes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.lang.reflect.Field;

import org.junit.jupiter.api.Test;

/**
 * Unit tests for the Changelog class
 * Tests changelog creation and field tracking
 */
public class ChangelogTest {

    @Test
    public void testChangelogCreationWithAllFields() throws NoSuchFieldException {
        // Arrange
        User user = new User();
        user.setName("TestUser");
        user.setId(1L);
        
        Sponsor sponsor = new Sponsor();
        sponsor.setName("TestSponsor");
        
        Field field = Sponsor.class.getDeclaredField("name");
        String beforeValue = "OldName";
        String afterValue = "NewName";
        
        // Act
        Changelog changelog = new Changelog(user, sponsor, field, beforeValue, afterValue);
        
        // Assert
        assertNotNull(changelog);
        assertEquals("name", changelog.getField());
        assertEquals(beforeValue, changelog.getBefore());
        assertEquals(afterValue, changelog.getAfter());
    }

    @Test
    public void testChangelogWithNullField() {
        // Arrange
        User user = new User();
        user.setName("TestUser");
        
        Sponsor sponsor = new Sponsor();
        sponsor.setName("TestSponsor");
        String beforeValue = "OldValue";
        String afterValue = "NewValue";
        
        // Act
        Changelog changelog = new Changelog(user, sponsor, null, beforeValue, afterValue);
        
        // Assert
        assertEquals("-", changelog.getField());
        assertEquals(beforeValue, changelog.getBefore());
        assertEquals(afterValue, changelog.getAfter());
    }

    @Test
    public void testChangelogWithNullBeforeValue() throws NoSuchFieldException {
        // Arrange
        User user = new User();
        user.setName("TestUser");
        Sponsor sponsor = new Sponsor();
        sponsor.setName("TestSponsor");
        Field field = Sponsor.class.getDeclaredField("name");
        String afterValue = "NewValue";
        
        // Act
        Changelog changelog = new Changelog(user, sponsor, field, null, afterValue);
        
        // Assert
        assertEquals("name", changelog.getField());
        assertEquals("-", changelog.getBefore());
        assertEquals(afterValue, changelog.getAfter());
    }

    @Test
    public void testChangelogWithNullAfterValue() throws NoSuchFieldException {
        // Arrange
        User user = new User();
        user.setName("TestUser");
        Sponsor sponsor = new Sponsor();
        sponsor.setName("TestSponsor");
        Field field = Sponsor.class.getDeclaredField("name");
        String beforeValue = "OldValue";
        
        // Act
        Changelog changelog = new Changelog(user, sponsor, field, beforeValue, null);
        
        // Assert
        assertEquals("name", changelog.getField());
        assertEquals(beforeValue, changelog.getBefore());
        assertEquals("-", changelog.getAfter());
    }

    @Test
    public void testChangelogWithEmptyStringBeforeValue() throws NoSuchFieldException {
        // Arrange
        User user = new User();
        user.setName("TestUser");
        Sponsor sponsor = new Sponsor();
        sponsor.setName("TestSponsor");
        Field field = Sponsor.class.getDeclaredField("name");
        String afterValue = "NewValue";
        
        // Act
        Changelog changelog = new Changelog(user, sponsor, field, "", afterValue);
        
        // Assert
        assertEquals("name", changelog.getField());
        assertEquals("-", changelog.getBefore());
        assertEquals(afterValue, changelog.getAfter());
    }

    @Test
    public void testChangelogWithEmptyStringAfterValue() throws NoSuchFieldException {
        // Arrange
        User user = new User();
        user.setName("TestUser");
        Sponsor sponsor = new Sponsor();
        sponsor.setName("TestSponsor");
        Field field = Sponsor.class.getDeclaredField("name");
        String beforeValue = "OldValue";
        
        // Act
        Changelog changelog = new Changelog(user, sponsor, field, beforeValue, "");
        
        // Assert
        assertEquals("name", changelog.getField());
        assertEquals(beforeValue, changelog.getBefore());
        assertEquals("-", changelog.getAfter());
    }

    @Test
    public void testChangelogWithAllNullValues() {
        // Arrange
        User user = new User();
        user.setName("TestUser");
        Sponsor sponsor = new Sponsor();
        sponsor.setName("TestSponsor");
        
        // Act
        Changelog changelog = new Changelog(user, sponsor, null, null, null);
        
        // Assert
        assertEquals("-", changelog.getField());
        assertEquals("-", changelog.getBefore());
        assertEquals("-", changelog.getAfter());
    }

    @Test
    public void testChangelogWithDifferentObjectType() throws NoSuchFieldException {
        // Arrange
        User user = new User();
        user.setName("TestUser");
        Contract contract = new Contract();
        contract.setName("TestContract");
        Field field = Contract.class.getDeclaredField("type");
        String beforeValue = "Basic";
        String afterValue = "Premium";
        
        // Act
        Changelog changelog = new Changelog(user, contract, field, beforeValue, afterValue);
        
        // Assert
        assertEquals("type", changelog.getField());
        assertEquals(beforeValue, changelog.getBefore());
        assertEquals(afterValue, changelog.getAfter());
    }

}
