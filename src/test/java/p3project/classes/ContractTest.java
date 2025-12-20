package p3project.classes;

import java.time.LocalDate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.jupiter.api.Test;

public class ContractTest {
    
    @Test
    public void testContractCreation(){

        Long id = (long) 222;
        LocalDate startDate = LocalDate.of(2002, 10, 20);
        LocalDate endDate = LocalDate.of(2025, 10, 20);
        String payment = "100";
        boolean status = false;
        String type = "type";

        Contract contract = new Contract();

        contract.setType(type);
        contract.setStartDate(startDate);
        contract.setEndDate(endDate);
        contract.setPayment(payment);
        contract.setActive(status);

        assertEquals(type, contract.getType());
        assertEquals(startDate, contract.getStartDate());
        assertEquals(endDate, contract.getEndDate());
        assertEquals(payment, contract.getPayment());

    }

    @Test
    public void testContractWithNullValues(){
        Contract contract = new Contract();

        assertNull(contract.getType());
        assertNull(contract.getStartDate());
        assertNull(contract.getEndDate());
        assertEquals(0, contract.getPaymentAsInt());

    }

}
