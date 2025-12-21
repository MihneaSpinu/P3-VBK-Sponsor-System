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

        User user = new User();
        user.setName("John Doe");
        user.setPassword("johnDoe123");

        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("John Doe");
    }
    @Test
    void testFindById() {

        User user = new User();
        user.setName("Jane Smith");
        User persistedUser = entityManager.persistAndFlush(user);
        
        Optional<User> foundUser = userRepository.findById(persistedUser.getId());
        
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Jane Smith");
    }
    
    @Test
    void testFindByName() {

        User user = new User();
        user.setName("Alice Brown");
        entityManager.persistAndFlush(user);
        
        User foundUser = userRepository.findByName("Alice Brown");
        
        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getName()).isEqualTo("Alice Brown");
    }
    
    @Test
    void testFindByNameNotFound() {

        User foundUser = userRepository.findByName("Nonexistent User");
        
        assertThat(foundUser).isNull();
    }
    
    @Test
    void testFindAll() {

        User user1 = new User();
        user1.setName("User One");
        entityManager.persistAndFlush(user1);
        
        User user2 = new User();
        user2.setName("User Two");
        entityManager.persistAndFlush(user2);
        
        List<User> users = userRepository.findAll();
        
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getName).containsExactlyInAnyOrder("User One", "User Two");
    }
    
    @Test
    void testUpdateUser() {

        User user = new User();
        user.setName("Original Name");
        User savedUser = entityManager.persistAndFlush(user);
        
        savedUser.setName("Updated Name");
        User updatedUser = userRepository.save(savedUser);
        
        assertThat(updatedUser.getId()).isEqualTo(savedUser.getId());
        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
    }
    
    @Test
    void testDeleteUser() {

        User user = new User();
        user.setName("To Delete");
        User savedUser = entityManager.persistAndFlush(user);
        Long userId = savedUser.getId();
        
        userRepository.deleteById(userId);
        
        Optional<User> deletedUser = userRepository.findById(userId);
        assertThat(deletedUser).isNotPresent();
    }
    
    @Test
    void testCount() {

        User user1 = new User();
        user1.setName("Count User 1");
        entityManager.persistAndFlush(user1);
        
        User user2 = new User();
        user2.setName("Count User 2");
        entityManager.persistAndFlush(user2);
        
        long count = userRepository.count();
        
        assertThat(count).isEqualTo(2);
    }
}
