package p3project.repositories;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import p3project.classes.User;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private UserRepository userRepository;

    @Test
    void testSaveUser() {
        // Given
        User user = new User();
        user.setName("John Doe");
        user.setPassword("johnDoe123");

        // When
        User savedUser = userRepository.save(user);

        // Then
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("John Doe");
        //assertThat(savedUser.getEmail()).isEqualTo("john.doe@example.com");
    }
    @Test
    void testFindById() {
        // Given
        User user = new User();
        user.setName("Jane Smith");
        //user.setEmail("jane.smith@example.com");
        User persistedUser = entityManager.persistAndFlush(user);
        
        // When
        Optional<User> foundUser = userRepository.findById(persistedUser.getId());
        
        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Jane Smith");
        //assertThat(foundUser.get().getEmail()).isEqualTo("jane.smith@example.com");
    }
    
    @Test
    void testFindByName() {
        // Given
        User user = new User();
        user.setName("Alice Brown");
        //user.setEmail("alice.brown@example.com");
        entityManager.persistAndFlush(user);
        
        // When
        User foundUser = userRepository.findByName("Alice Brown");
        
        // Then
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getName()).isEqualTo("Alice Brown");
        //assertThat(foundUser.getEmail()).isEqualTo("alice.brown@example.com");
    }
    
    @Test
    void testFindByNameNotFound() {
        // When
        User foundUser = userRepository.findByName("Nonexistent User");
        
        // Then
        assertThat(foundUser).isNull();
    }
    
    @Test
    void testFindAll() {
        // Given
        User user1 = new User();
        user1.setName("User One");
        //user1.setEmail("user1@example.com");
        entityManager.persistAndFlush(user1);
        
        User user2 = new User();
        user2.setName("User Two");
        //user2.setEmail("user2@example.com");
        entityManager.persistAndFlush(user2);
        
        // When
        List<User> users = userRepository.findAll();
        
        // Then
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getName).containsExactlyInAnyOrder("User One", "User Two");
    }
    
    @Test
    void testUpdateUser() {
        // Given
        User user = new User();
        user.setName("Original Name");
        //user.setEmail("original@example.com");
        User savedUser = entityManager.persistAndFlush(user);
        
        // When
        savedUser.setName("Updated Name");
        //savedUser.setEmail("updated@example.com");
        User updatedUser = userRepository.save(savedUser);
        
        // Then
        assertThat(updatedUser.getId()).isEqualTo(savedUser.getId());
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        //assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
    }
    
    @Test
    void testDeleteUser() {
        // Given
        User user = new User();
        user.setName("To Delete");
        //user.setEmail("delete@example.com");
        User savedUser = entityManager.persistAndFlush(user);
        Long userId = savedUser.getId();
        
        // When
        userRepository.deleteById(userId);
        
        // Then
        Optional<User> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isNotPresent();
    }
    
    @Test
    void testCount() {
        // Given
        User user1 = new User();
        user1.setName("Count User 1");
        //user1.setEmail("count1@example.com");
        entityManager.persistAndFlush(user1);
        
        User user2 = new User();
        user2.setName("Count User 2");
        //user2.setEmail("count2@example.com");
        entityManager.persistAndFlush(user2);
        
        // When
        long count = userRepository.count();
        
        // Then
        assertThat(count).isEqualTo(2);
    }
}
