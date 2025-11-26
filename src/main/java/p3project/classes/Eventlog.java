package p3project.classes;

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;

import jakarta.persistence.*;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // Én tabel til hele klasse hierarkiet

// Laver kolonnen "logType" i databasen, der gør at Hibernate kan kende forskel
// på de to klasser
@DiscriminatorColumn(name = "logType", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("Eventlog") // "Eventlog" bliver værdien til "logType" kolonnen
public class Eventlog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String username;
    private String objectType;
    private String objectName;
    private LocalDateTime timestamp;
    private String action; // String måske?

    protected Eventlog() {};

    public <T> Eventlog(User user, T changedObject, String action) {
        this.username = "Test";

        this.objectType = changedObject.getClass().getSimpleName(); // oversæt til dansk på frontend xd
        /*
        try {
            Method getName = changedObject.getClass().getMethod("getName"); // jank
            this.objectName = getName.invoke(changedObject).toString();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException error) {
            this.objectName = "-";
            throw new RuntimeException("Error getting name of target object: ", error);
        }
        */
        this.objectName = "testObjectName";

        this.timestamp = LocalDateTime.now(); // skal formateres ordentligt
        this.action = action;
    }

    // Getters used exclusively for dynamic Thymeleaf view generation
    public String getUsername() {
        return this.username;
    }

    public String getObjectType() {
        return this.objectType;
    }

    public String getObjectName() {
        return this.objectName;
    }

    public LocalDateTime getTimestamp() {
        return this.timestamp;
    }

    public String getAction() {
        return this.action;
    }



    /*
    // legacy kode
    public static <T> Eventlog create(User user, T changedObject, String action) {
        Eventlog log = new Eventlog();
        log.username = "testUsername";
        
        //log.objectType = changedObject.getClass().getSimpleName(); // oversæt til dansk på frontend xd
        try {
            Method getName = changedObject.getClass().getMethod("getName"); // jank
            log.objectName = getName.invoke(changedObject).toString();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException error) {
            log.objectName = "-";
            throw new RuntimeException("Error getting name of target object: ", error);
        }
        
        log.timestamp = LocalDateTime.now(); // skal formateres ordentligt
        log.action = action;
        return log;
    }
    */
}
