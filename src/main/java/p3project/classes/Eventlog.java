package p3project.classes;

import java.util.Date; // date, localdate, time?

import jakarta.persistence.DiscriminatorColumn; // static for ikke at skrive Action.CREATE etc...
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;

@Entity
@Inheritance(strategy = InheritanceType.SINGLE_TABLE) // Én tabel til hele klasse hierarkiet

// Laver kolonnen "logType" i databasen, der gør at Hibernate kan kende forskel på de to klasser
@DiscriminatorColumn(name="logType", discriminatorType = DiscriminatorType.STRING)
@DiscriminatorValue("Eventlog") // "Eventlog" bliver værdien til "logType" kolonnen
public class Eventlog {
    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Integer id;
    private String username;
    private String objectType; // .getClass().getSimpleName();
    private String objectName;
    private Date timestamp;
    private int action; // String måske?

    protected Eventlog() {}
    /*
    public Eventlog(User user, String objectType, String objectName, Action action) {
        this.username = user.getName();
        this.objectType = objectType.getClass().getSimpleName();
        this.objectName = objectName;
        this.timestamp = new Date();
        this.action = action;
    }
    */

    public Integer getId() {
        return this.id;
    }


    public static Eventlog create(User user, Object objectType, String objectName, int action) {
        Eventlog log = new Eventlog();
        log.username = user.getName();
        log.objectType = objectType.getClass().getSimpleName(); // oversæt til dansk på frontend xd
        log.objectName = objectName;
        log.timestamp = new Date();
        log.action = action;
        return log;
    }
}

