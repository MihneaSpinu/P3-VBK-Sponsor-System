package p3project.classes;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class ServiceTest {

    @Test
    public void testServiceCreation() {
        Service service = new Service("Tickets", true, 20);

        service.setDivision(10);
        service.setType("Banner");
        service.setActive(false);

        assertEquals(false, service.getActive());
        assertEquals("Banner", service.getType());
        assertEquals(10, service.getDivision());
    }

    @Test
    public void testServiceActiveFlag() {
        LocalDate now = LocalDate.of(2025, 1, 1);
        LocalDate beforeNow = LocalDate.of(2020, 1, 1);
        Service service = new Service(
            1L,
            "Yes",
            "Banner",
            true,
            beforeNow,
            now,
            20,
            0
        );

        boolean isActive = service.getActive();
        assertEquals(true, isActive);

        service.setType("Biletter");
        service.setActive(false);

        isActive = service.getActive();
        assertEquals(false, isActive);
    }
}
