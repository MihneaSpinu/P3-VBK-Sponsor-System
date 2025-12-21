package p3project.classes;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import org.junit.jupiter.api.Test;


public class UserTest {

    @Test
    public void testUserCreation() {
        // Arrange
        User user = new User();
        
        // Act
        user.setName("John Doe");
        user.setPassword("john123");
        user.setId(1L);
        
        // Assert
        assertEquals("John Doe", user.getName());
        assertEquals("john123", user.getPassword());
        assertEquals(1L, user.getId());
    }
    
    @Test
    public void testUserWithNullValues() {
        // Arrange
        User user = new User();
                
        // Assert
        assertNull(user.getName());
        assertNull(user.getId());
    }
}