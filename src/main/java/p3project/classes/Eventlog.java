package p3project.classes;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import jakarta.persistence.DiscriminatorColumn; // date, localdate, time?
import jakarta.persistence.DiscriminatorType; // static for ikke at skrive Action.CREATE etc...
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

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
    private String timestamp;
    private String action; // String måske?

    protected Eventlog() {};

    public <T> Eventlog(User user, T targetObject, String action) {
        this.username = user.getName(); 
        this.objectType = targetObject.getClass().getSimpleName(); // oversæt til dansk på frontend xd
        try {
            Method getName = targetObject.getClass().getMethod("getName"); // jank
            this.objectName = getName.invoke(targetObject).toString();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException error) {
            this.objectName = "-";
        }

        LocalDateTime unformattedTimestamp = LocalDateTime.now();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("dd/MM-yyyy  HH:mm");
        this.timestamp = unformattedTimestamp.format(format);
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

    public String getTimestamp() {
        return this.timestamp;
    }

    public String getAction() {
        return this.action;
    }

}
