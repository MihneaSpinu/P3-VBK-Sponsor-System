package p3project.classes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for the User class
 * These test the basic functionality of User objects
 */
public class UserTest {

    @Test
    public void testUserCreation() {
        // Arrange - Create a new user
        User user = new User();
        
        // Act - Set properties
        user.setName("John Doe");
        user.setEmail("john@example.com");
        user.setId(1);
        
        // Assert - Check that properties were set correctly
        assertEquals("John Doe", user.getName());
        assertEquals("john@example.com", user.getEmail());
        assertEquals(1, user.getId());
    }
    
    @Test
    public void testUserWithNullValues() {
        // Arrange
        User user = new User();
        
        // Act - Don't set any properties
        
        // Assert - Should handle null values gracefully
        assertNull(user.getName());
        assertNull(user.getEmail());
        assertNull(user.getId());
    }
    
    @Test
    public void testUserEmailValidation() {
        // Arrange
        User user = new User();
        
        // Act
        user.setEmail("valid@email.com");
        
        // Assert
        assertTrue(user.getEmail().contains("@"));
        assertTrue(user.getEmail().contains("."));
    }
}