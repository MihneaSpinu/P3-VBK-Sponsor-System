package p3project.classes;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import static p3project.classes.Action.*;

import java.util.Date;

@Entity
@DiscriminatorValue("Changelog") // "Changelog" bliver værdien til "logType" kolonnen
public class Changelog extends Eventlog {
    // changed code: avoid reserved words by mapping to safe column names
    @Column(name = "before_value")
    private String before;

    @Column(name = "after_value")
    private String after;

    // ikke @Override
    public static Changelog create(User user, String objectType, String objectName, String before, String after) {
        Changelog log = (Changelog)Eventlog.create(user, objectType, objectName, UPDATE);
        log.before = before;
        log.after = after;
        return log;
    }

}
