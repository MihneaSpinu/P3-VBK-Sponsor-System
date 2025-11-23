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
    private Integer id;
    private String username;
    private String objectType; // .getClass().getSimpleName();
    private String objectName;
    private LocalDateTime timestamp;
    private String action; // String måske?

    protected Eventlog() {}
    /*
     * public Eventlog(User user, String objectType, String objectName, Action action) {
     * this.username = user.getName();
     * this.objectType = objectType.getClass().getSimpleName();
     * this.objectName = objectName;
     * this.timestamp = new Date();
     * this.action = action;
     * }
     */

    public Integer getId() {
        return this.id;
    }

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

    // sæt ind i constructor?
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
}
