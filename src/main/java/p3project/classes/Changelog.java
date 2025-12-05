package p3project.classes;

import java.lang.reflect.Field;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.DiscriminatorValue;


@Entity
@DiscriminatorValue("Changelog") // "Changelog" bliver værdien til "logType" kolonnen
public class Changelog extends Eventlog {

    protected Changelog() {};

    public <T> Changelog(User user, T changedObject, Field field, Object before, Object after) {
        super(user, changedObject, "UPDATED");

        this.field = field == null ? "-" : field.getName();
        this.before = before == null ? "-" : before.toString();
        this.after = after == null ? "-" : after.toString();
    }

    private String field;

    @Column(name = "before_value")
    private String before;

    @Column(name = "after_value")
    private String after;

    public String getField() {
        return this.field;
    }

    public String getBefore() {
        return this.before;
    }

    public String getAfter() {
        return this.after;
    }

    /* legacy kode
    public static <T> Changelog create(User user, T changedObject, Field field, Object before, Object after) {
        Changelog log = (Changelog)Eventlog.create(user, changedObject, "UPDATE");
        log.field = field.getName();
        log.before = before.toString();
        log.after = after.toString();
        return log;
    }
    */

}
