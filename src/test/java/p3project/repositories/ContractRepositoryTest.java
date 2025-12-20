package p3project.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import p3project.classes.Contract;

@DataJpaTest
class ContractRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ContractRepository contractRepository;

    @Test
    void testSaveContract() {

        Contract contract = new Contract(
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31),
            "50000",
            true,
            "Premium"
        );
        contract.setSponsorId(1L);

        Contract savedContract = contractRepository.save(contract);

        assertThat(savedContract.getId()).isNotNull();
        assertThat(savedContract.getStartDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(savedContract.getEndDate()).isEqualTo(LocalDate.of(2024, 12, 31));
        assertThat(savedContract.getPaymentAsInt()).isEqualTo(50000);
        assertThat(savedContract.getActive()).isTrue();
        assertThat(savedContract.getType()).isEqualTo("Premium");
        assertThat(savedContract.getSponsorId()).isEqualTo(1L);

    }

    @Test
    void testFindById() {

        Contract contract = new Contract(
            LocalDate.of(2023, 6, 1),
            LocalDate.of(2023, 12, 31),
            "25000",
            false,
            "Standard"
        );
        contract.setSponsorId(2L);

        Contract persistedContract = entityManager.persistAndFlush(contract);

        Optional<Contract> foundContract = contractRepository.findById(persistedContract.getId());

        assertThat(foundContract).isPresent();
        assertThat(foundContract.get().getStartDate()).isEqualTo(LocalDate.of(2023, 6, 1));
        assertThat(foundContract.get().getPaymentAsInt()).isEqualTo(25000);
        assertThat(foundContract.get().getActive()).isFalse();
        assertThat(foundContract.get().getType()).isEqualTo("Standard");
    }

    @Test
    void testFindAll() {
 
        Contract contract1 = new Contract(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), "30000", true, "Type A");
        Contract contract2 = new Contract(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 11, 30), "40000", true, "Type B");

        entityManager.persistAndFlush(contract1);
        entityManager.persistAndFlush(contract2);

        List<Contract> contracts = contractRepository.findAll();

        assertThat(contracts).hasSize(2);
    }

    @Test
    void testUpdateContract() {

        Contract contract = new Contract(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), "10000", true, "Basic");
        contract.setSponsorId(1L);
        Contract savedContract = entityManager.persistAndFlush(contract);

        savedContract.setPayment("20000");
        savedContract.setType("Premium");
        savedContract.setActive(false);

        Contract updatedContract = contractRepository.save(savedContract);

        assertThat(updatedContract.getId()).isEqualTo(savedContract.getId());
        assertThat(updatedContract.getPaymentAsInt()).isEqualTo(20000);
        assertThat(updatedContract.getType()).isEqualTo("Premium");
        assertThat(updatedContract.getActive()).isFalse();

    }

    @Test
    void testDeleteContract() {

        Contract contract = new Contract(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), "15000", true, "Delete");
        Contract savedContract = entityManager.persistAndFlush(contract);
        Long contractId = savedContract.getId();

        contractRepository.deleteById(contractId);

        Optional<Contract> deletedContract = contractRepository.findById(contractId);
        assertThat(deletedContract).isNotPresent();
    }

    @Test
    void testCount() {

        Contract contract1 = new Contract(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), "10000", true, "Type 1");
        Contract contract2 = new Contract(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 11, 30), "20000", true, "Type 2");
        Contract contract3 = new Contract(LocalDate.of(2024, 3, 1), LocalDate.of(2024, 10, 31), "30000", false, "Type 3");
        entityManager.persistAndFlush(contract1);
        entityManager.persistAndFlush(contract2);
        entityManager.persistAndFlush(contract3);

        long count = contractRepository.count();

        assertThat(count).isEqualTo(3);
    }

    @Test
    void testSaveContractWithPdfData() {

        Contract contract = new Contract(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), "50000", true, "Premium");
        byte[] pdfData = "Sample PDF Data".getBytes();
        contract.setPdfData(pdfData);

        Contract savedContract = contractRepository.save(contract);

        assertThat(savedContract.getId()).isNotNull();
        assertThat(savedContract.getPdfData()).isEqualTo(pdfData);
    }

    @Test
    void testSaveContractWithEmptyConstructor() {

        Contract contract = new Contract();
        contract.setStartDate(LocalDate.of(2024, 5, 1));
        contract.setEndDate(LocalDate.of(2024, 12, 31));
        contract.setPayment("35000");
        contract.setActive(true);
        contract.setType("Custom");
        contract.setSponsorId(5L);

        Contract savedContract = contractRepository.save(contract);

        assertThat(savedContract.getId()).isNotNull();
        assertThat(savedContract.getStartDate()).isEqualTo(LocalDate.of(2024, 5, 1));
        assertThat(savedContract.getPaymentAsInt()).isEqualTo(35000);

    }

    @Test
    void testUpdateContractDates() {

        Contract contract = new Contract(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30), "10000", true, "Short Term");
        Contract savedContract = entityManager.persistAndFlush(contract);

        savedContract.setEndDate(LocalDate.of(2024, 12, 31));
        Contract updatedContract = contractRepository.save(savedContract);

        assertThat(updatedContract.getEndDate()).isEqualTo(LocalDate.of(2024, 12, 31));
        assertThat(updatedContract.getStartDate()).isEqualTo(LocalDate.of(2024, 1, 1));
    }
}
