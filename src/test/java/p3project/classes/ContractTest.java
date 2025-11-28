package p3project.classes;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.jupiter.api.Test;

public class ContractTest {
    
    @Test
    public void testContractCreation(){
        //Variables
        Long id = (long) 222;
        LocalDate startDate = LocalDate.of(2002, 10, 20);
        LocalDate endDate = LocalDate.of(2025, 10, 20);
        int payment = 100;
        boolean status = false;
        String type = "type";

        Contract contract = new Contract();

        //set Variables
        //contract.setId(id) //no set id
        contract.setType(type);
        contract.setStartDate(startDate);
        contract.setEndDate(endDate);
        contract.setPayment(payment);
        contract.setStatus(status);


        //assert
        assertEquals(type, contract.getType());
        assertEquals(startDate, contract.getStartDate());
        assertEquals(endDate, contract.getEndDate());
        assertEquals(payment, contract.getPayment());
        //assertEquals(status, contract.getStatus)); no get status

    }

    @Test
    public void testContractWithNullValues(){
        Contract contract = new Contract();

        assertNull(contract.getType());
        assertNull(contract.getStartDate());
        assertNull(contract.getEndDate());
        assertEquals(0, contract.getPayment()); //Int by default is 0. When no values givin its 0.
        //assertEquals(status, contract.getStatus)); no get status

    }

}
