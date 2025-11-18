package p3project.repositories;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import p3project.classes.User;

/**
 * Repository tests for UserRepository
 * @DataJpaTest provides a test database and Spring Data JPA testing support
 */
@DataJpaTest
public class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager; // For direct database operations in tests
    
    @Autowired
    private UserRepository userRepository;
    
    @Test
    public void testSaveAndFindUser() {
        // Arrange - Create a user
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        
        // Act - Save the user
        User savedUser = userRepository.save(user);
        
        // Assert - Check that user was saved with an ID
        assertNotNull(savedUser.getId());
        assertEquals("Test User", savedUser.getName());
        assertEquals("test@example.com", savedUser.getEmail());
    }
    
    @Test
    public void testFindAllUsers() {
        // Arrange - Create multiple users
        User user1 = new User();
        user1.setName("User 1");
        user1.setEmail("user1@example.com");
        
        User user2 = new User();
        user2.setName("User 2");
        user2.setEmail("user2@example.com");
        
        // Act - Save users and find all
        userRepository.save(user1);
        userRepository.save(user2);
        Iterable<User> users = userRepository.findAll();
        
        // Assert - Check that both users are found
        long count = 0;
        for (User user : users) {
            count++;
        }
        assertEquals(2, count);
    }
    
    @Test
    public void testDeleteUser() {
        // Arrange - Create and save a user
        User user = new User();
        user.setName("Delete Me");
        user.setEmail("delete@example.com");
        User savedUser = userRepository.save(user);
        
        // Act - Delete the user
        userRepository.deleteById(savedUser.getId());
        
        // Assert - User should no longer exist
        assertFalse(userRepository.findById(savedUser.getId()).isPresent());
    }
}