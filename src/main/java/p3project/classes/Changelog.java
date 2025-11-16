package p3project.classes;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

import java.util.Date;

@Entity
@DiscriminatorValue("Changelog") // "Changelog" bliver værdien til "logType" kolonnen
public class Changelog extends Eventlog {
    private String before;
    private String after;

    // ikke @Override
    public static Changelog create(User user, String objectType, String objectName, Action action, String before, String after) {
        Changelog log = (Changelog)Eventlog.create(user, objectType, objectName, action);
        log.before = before;
        log.after = after;
        return log;
    }

}
