package p3project.classes;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class ServiceTest {
    
    /*
    @Test
    public void testServiceCreation(){
        Service service = new Service(ServiceType.Tickets, true, 20);
        
        service.setAmountOrDuration(10);
        service.setType(ServiceType.Banner);
        service.setStatus(false);
        
        assertEquals(false, service.getStatus());
        assertEquals(ServiceType.Banner, service.getType());
        assertEquals(10, service.getAmountOrDuration());
        
    }
    @Test
    public void testServiceIsArchived(){
        LocalDate now = LocalDate.of(2025, 1, 1);
        LocalDate beforeNow = LocalDate.of(2020, 1, 1);
        Service service = new Service(1L,
        "Yes",
        "Banner",
        false, 
        20, 
        beforeNow,
        now);
        
        boolean isActive = service.isActive();
        
        assertEquals(true, isActive);
        
        service.setType("Biletter");
        service.setArchived(false);
        
        isActive = service.isActive();
        
        assertEquals(false, isActive);
        
        

    }
    
    */
    
}
