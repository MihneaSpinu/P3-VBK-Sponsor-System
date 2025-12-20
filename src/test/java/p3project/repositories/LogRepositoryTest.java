
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
        user.setPassword("test123");
        entityManager.persistAndFlush(user);
        
        Object testObject = new Object();
        Eventlog log = new Eventlog(user, testObject, "Test Object");
        
        // When
        Eventlog savedLog = logRepository.save(log);
        
        // Then
        assertThat(savedLog).isNotNull();
    }
    
    @Test
    void testFindById() {
        // Given
        User user = new User();
        user.setName("Find User");
        user.setPassword("find123");
        entityManager.persistAndFlush(user);
        
        Object testObject = new String("Test String");
        Eventlog log = new Eventlog(user, testObject, "Test String Object");
        Eventlog persistedLog = entityManager.persistAndFlush(log);
        
        // When
        Optional<Eventlog> foundLog = logRepository.findById(entityManager.getId(persistedLog, Long.class));
        
        // Then
        assertThat(foundLog).isPresent();
        Long persistedId = entityManager.getId(persistedLog, Long.class);
        Long foundId = entityManager.getId(foundLog.get(), Long.class);
        assertThat(foundId).isEqualTo(persistedId);
    }
    
    @Test
    void testFindAll() {
        // Given
        User user1 = new User();
        user1.setName("User One");
        // No email field on User in domain model
        entityManager.persistAndFlush(user1);
        
        User user2 = new User();
        user2.setName("User Two");
        // No email field on User in domain model
        entityManager.persistAndFlush(user2);
        
        Object obj1 = new Object();
        Object obj2 = new Object();
        Eventlog log1 = new Eventlog(user1, obj1, "Object 1");
        Eventlog log2 = new Eventlog(user2, obj2, "Object 2");
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
        // No email field on User in domain model
        entityManager.persistAndFlush(user);
        
        Object testObject = new Object();
        Eventlog log = new Eventlog(user, testObject, "To Delete");
        Eventlog savedLog = entityManager.persistAndFlush(log);
        Long logId = entityManager.getId(savedLog, Long.class);
        
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
        // No email field on User in domain model
        entityManager.persistAndFlush(user);
        
        Object obj1 = new Object();
        Object obj2 = new Object();
        Object obj3 = new Object();
        Eventlog log1 = new Eventlog(user, obj1, "Log 1");
        Eventlog log2 = new Eventlog(user, obj2, "Log 2");
        Eventlog log3 = new Eventlog(user, obj3, "Log 3");
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
        // No email field on User in domain model
        entityManager.persistAndFlush(user);
        
        String stringObj = "Test String";
        Integer intObj = 42;
        
        Eventlog log1 = new Eventlog(user, stringObj, "String Object");
        Eventlog log2 = new Eventlog(user, intObj, "Integer Object");
        
        // When
        Eventlog savedLog1 = logRepository.save(log1);
        Eventlog savedLog2 = logRepository.save(log2);
        
        // Then
        Long id1 = entityManager.getId(savedLog1, Long.class);
        Long id2 = entityManager.getId(savedLog2, Long.class);
        assertThat(id1).isNotNull();
        assertThat(id2).isNotNull();
    }
    
    @Test
    void testSaveMultipleLogsForSameUser() {
        // Given
        User user = new User();
        user.setName("Active User");
        // No email field on User in domain model
        entityManager.persistAndFlush(user);
        
        Object obj1 = new Object();
        Object obj2 = new Object();
        Object obj3 = new Object();
        
        Eventlog log1 = new Eventlog(user, obj1, "Action 1");
        Eventlog log2 = new Eventlog(user, obj2, "Action 2");
        Eventlog log3 = new Eventlog(user, obj3, "Action 3");
        
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
        // No email field on User in domain model
        entityManager.persistAndFlush(user);
        
        Object obj = new Object();
        Eventlog createLog = new Eventlog(user, obj, "CREATE");
        Eventlog updateLog = new Eventlog(user, obj, "UPDATE");
        Eventlog deleteLog = new Eventlog(user, obj, "DELETE");
        
        // When
        Eventlog savedCreate = logRepository.save(createLog);
        Eventlog savedUpdate = logRepository.save(updateLog);
        Eventlog savedDelete = logRepository.save(deleteLog);
        
        // Then
        assertThat(entityManager.getId(savedCreate, Long.class)).isNotNull();
        assertThat(entityManager.getId(savedUpdate, Long.class)).isNotNull();
        assertThat(entityManager.getId(savedDelete, Long.class)).isNotNull();
    }
}