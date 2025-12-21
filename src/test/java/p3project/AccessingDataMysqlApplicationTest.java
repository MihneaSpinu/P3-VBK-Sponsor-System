package p3project;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;


@SpringBootTest
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb", // Use in-memory database for testing
    "spring.jpa.hibernate.ddl-auto=create-drop"
})
public class AccessingDataMysqlApplicationTest {

    @Test
    public void contextLoads() {}
    
}