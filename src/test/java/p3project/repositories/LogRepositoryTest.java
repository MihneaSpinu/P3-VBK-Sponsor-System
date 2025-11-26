package p3project.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import p3project.classes.Eventlog;
import p3project.classes.User;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class LogRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private LogRepository logRepository;

    @Test
    void testSaveEventlog() {
        // Given
        User user = new User();
        user.setName("Test User");
        user.setEmail("test@example.com");
        entityManager.persistAndFlush(user);

        Object testObject = new Object();
        Eventlog log = Eventlog.create(user, testObject, "Test Object", 1);

        // When
        Eventlog savedLog = logRepository.save(log);

        // Then
        assertThat(savedLog.getId()).isNotNull();
    }

    @Test
    void testFindById() {
        // Given
        User user = new User();
        user.setName("Find User");
        user.setEmail("find@example.com");
        entityManager.persistAndFlush(user);

        Object testObject = new String("Test String");
        Eventlog log = Eventlog.create(user, testObject, "Test String Object", 2);
        Eventlog persistedLog = entityManager.persistAndFlush(log);

        // When
        Optional<Eventlog> foundLog = logRepository.findById(persistedLog.getId());

        // Then
        assertThat(foundLog).isPresent();
        assertThat(foundLog.get().getId()).isEqualTo(persistedLog.getId());
    }

    @Test
    void testFindAll() {
        // Given
        User user1 = new User();
        user1.setName("User One");
        user1.setEmail("user1@example.com");
        entityManager.persistAndFlush(user1);

        User user2 = new User();
        user2.setName("User Two");
        user2.setEmail("user2@example.com");
        entityManager.persistAndFlush(user2);

        Object obj1 = new Object();
        Object obj2 = new Object();
        Eventlog log1 = Eventlog.create(user1, obj1, "Object 1", 1);
        Eventlog log2 = Eventlog.create(user2, obj2, "Object 2", 2);
        entityManager.persistAndFlush(log1);
        entityManager.persistAndFlush(log2);

        // When
        List<Eventlog> logs = logRepository.findAll();

        // Then
        assertThat(logs).hasSize(2);
    }

    @Test
    void testDeleteEventlog() {
        // Given
        User user = new User();
        user.setName("Delete User");
        user.setEmail("delete@example.com");
        entityManager.persistAndFlush(user);

        Object testObject = new Object();
        Eventlog log = Eventlog.create(user, testObject, "To Delete", 3);
        Eventlog savedLog = entityManager.persistAndFlush(log);
        Integer logId = savedLog.getId();

        // When
        logRepository.deleteById(logId);

        // Then
        Optional<Eventlog> deletedLog = logRepository.findById(logId);
        assertThat(deletedLog).isNotPresent();
    }

    @Test
    void testCount() {
        // Given
        User user = new User();
        user.setName("Count User");
        user.setEmail("count@example.com");
        entityManager.persistAndFlush(user);

        Object obj1 = new Object();
        Object obj2 = new Object();
        Object obj3 = new Object();
        Eventlog log1 = Eventlog.create(user, obj1, "Log 1", 1);
        Eventlog log2 = Eventlog.create(user, obj2, "Log 2", 2);
        Eventlog log3 = Eventlog.create(user, obj3, "Log 3", 3);
        entityManager.persistAndFlush(log1);
        entityManager.persistAndFlush(log2);
        entityManager.persistAndFlush(log3);

        // When
        long count = logRepository.count();

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    void testCreateEventlogWithDifferentObjectTypes() {
        // Given
        User user = new User();
        user.setName("Multi Type User");
        user.setEmail("multitype@example.com");
        entityManager.persistAndFlush(user);

        String stringObj = "Test String";
        Integer intObj = 42;
        
        Eventlog log1 = Eventlog.create(user, stringObj, "String Object", 1);
        Eventlog log2 = Eventlog.create(user, intObj, "Integer Object", 2);

        // When
        Eventlog savedLog1 = logRepository.save(log1);
        Eventlog savedLog2 = logRepository.save(log2);

        // Then
        assertThat(savedLog1.getId()).isNotNull();
        assertThat(savedLog2.getId()).isNotNull();
    }

    @Test
    void testSaveMultipleLogsForSameUser() {
        // Given
        User user = new User();
        user.setName("Active User");
        user.setEmail("active@example.com");
        entityManager.persistAndFlush(user);

        Object obj1 = new Object();
        Object obj2 = new Object();
        Object obj3 = new Object();
        
        Eventlog log1 = Eventlog.create(user, obj1, "Action 1", 1);
        Eventlog log2 = Eventlog.create(user, obj2, "Action 2", 2);
        Eventlog log3 = Eventlog.create(user, obj3, "Action 3", 3);

        // When
        logRepository.save(log1);
        logRepository.save(log2);
        logRepository.save(log3);
        List<Eventlog> allLogs = logRepository.findAll();

        // Then
        assertThat(allLogs).hasSize(3);
    }

    @Test
    void testEventlogWithDifferentActions() {
        // Given
        User user = new User();
        user.setName("Action User");
        user.setEmail("action@example.com");
        entityManager.persistAndFlush(user);

        Object obj = new Object();
        Eventlog createLog = Eventlog.create(user, obj, "Test Object", 1);  // CREATE action
        Eventlog updateLog = Eventlog.create(user, obj, "Test Object", 2);  // UPDATE action
        Eventlog deleteLog = Eventlog.create(user, obj, "Test Object", 3);  // DELETE action

        // When
        Eventlog savedCreate = logRepository.save(createLog);
        Eventlog savedUpdate = logRepository.save(updateLog);
        Eventlog savedDelete = logRepository.save(deleteLog);

        // Then
        assertThat(savedCreate.getId()).isNotNull();
        assertThat(savedUpdate.getId()).isNotNull();
        assertThat(savedDelete.getId()).isNotNull();
    }
}
