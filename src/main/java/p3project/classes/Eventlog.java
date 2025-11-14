package p3project.classes;

import java.util.Date; // date, localdate, time?
import static p3project.classes.Action.*; // static for ikke at skrive Action.CREATE etc...

import jakarta.persistence.*;

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
    private Action action; // String måske?

    protected Eventlog() {} // lav private constructor, med method som wrapper så changelog kan bruge samme, .create måske
    /*
    public Eventlog(User user, String objectType, String objectName, Action action) {
        this.username = user.getName();
        this.objectType = objectType.getClass().getSimpleName(); // oversæt til dansk xd
        this.objectName = objectName; // object.getName(); i stedet hvis generisk klasse
        this.timestamp = new Date();
        this.action = action;
    }
    */


    public static Eventlog create(User user, String objectType, String objectName, Action action) {
        Eventlog log = new Eventlog();
        log.username = user.getName();
        log.objectType = objectType.getClass().getSimpleName(); // oversæt til dansk xd
        log.objectName = objectName;
        log.timestamp = new Date();
        log.action = action;
        return log;
    }
}

