package p3project.classes;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class ServiceTest {

    @Test
    public void testServiceCreation() {
        Service service = new Service("Tickets", true, 20);

        service.setAmountOrDivision(10);
        service.setType("Banner");
        service.setActive(false);

        assertEquals(false, service.getActive());
        assertEquals("Banner", service.getType());
        assertEquals(10, service.getAmountOrDivision());
    }

    @Test
    public void testServiceActiveFlag() {
        LocalDate now = LocalDate.of(2025, 1, 1);
        LocalDate beforeNow = LocalDate.of(2020, 1, 1);
        Service service = new Service(
            1L,
            "Yes",
            "Banner",
            true, // start as active to reflect expected state
            20,
            beforeNow,
            now
        );

        boolean isActive = service.getActive();
        assertEquals(true, isActive);

        service.setType("Biletter");
        service.setActive(false);

        isActive = service.getActive();
        assertEquals(false, isActive);
    }
}
