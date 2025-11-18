package p3project;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

/**
 * Integration test for the entire application
 * @SpringBootTest starts the full application context
 */
@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb", // Use in-memory database for testing
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class AccessingDataMysqlApplicationTest {

    @Test
    public void contextLoads() {
        // This test verifies that the Spring application context can start up successfully
        // If this test passes, it means all your beans are configured correctly
    }
}