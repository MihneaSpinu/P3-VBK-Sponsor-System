package p3project.classes;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import static p3project.classes.Action.*;

import java.util.Date;

@Entity
@DiscriminatorValue("Changelog") // "Changelog" bliver værdien til "logType" kolonnen
public class Changelog extends Eventlog {
    private String before;
    private String after;

    // ikke @Override
    public static Changelog create(User user, String objectType, String objectName, String before, String after) {
        Changelog log = (Changelog)Eventlog.create(user, objectType, objectName, UPDATE);
        log.before = before;
        log.after = after;
        return log;
    }

}
