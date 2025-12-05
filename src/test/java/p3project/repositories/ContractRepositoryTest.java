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
        // Given
        Contract contract = new Contract(
            LocalDate.of(2024, 1, 1),
            LocalDate.of(2024, 12, 31),
            "50000",
            true,
            "Premium"
        );
        contract.setSponsorId(1L);


        // When
        Contract savedContract = contractRepository.save(contract);

        // Then
        assertThat(savedContract.getId()).isNotNull();
        assertThat(savedContract.getStartDate()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(savedContract.getEndDate()).isEqualTo(LocalDate.of(2024, 12, 31));
        assertThat(savedContract.getPayment()).isEqualTo(50000);
        assertThat(savedContract.isStatus()).isTrue();
        assertThat(savedContract.getType()).isEqualTo("Premium");
        assertThat(savedContract.getSponsorId()).isEqualTo(1L);

    }

    @Test
    void testFindById() {
        // Given
        Contract contract = new Contract(
            LocalDate.of(2023, 6, 1),
            LocalDate.of(2023, 12, 31),
            "25000",
            false,
            "Standard"
        );
        contract.setSponsorId(2L);

        Contract persistedContract = entityManager.persistAndFlush(contract);

        // When
        Optional<Contract> foundContract = contractRepository.findById(persistedContract.getId());

        // Then
        assertThat(foundContract).isPresent();
        assertThat(foundContract.get().getStartDate()).isEqualTo(LocalDate.of(2023, 6, 1));
        assertThat(foundContract.get().getPayment()).isEqualTo(25000);
        assertThat(foundContract.get().isStatus()).isFalse();
        assertThat(foundContract.get().getType()).isEqualTo("Standard");

    }

    @Test
    void testFindAll() {
        // Given
        Contract contract1 = new Contract(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), "30000", true, "Type A");

        
        Contract contract2 = new Contract(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 11, 30), "40000", true, "Type B");

        
        entityManager.persistAndFlush(contract1);
        entityManager.persistAndFlush(contract2);

        // When
        List<Contract> contracts = contractRepository.findAll();

        // Then
        assertThat(contracts).hasSize(2);

    }

    @Test
    void testUpdateContract() {
        // Given
        Contract contract = new Contract(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), "10000", true, "Basic");
        contract.setSponsorId(1L);

        Contract savedContract = entityManager.persistAndFlush(contract);

        // When
        savedContract.setPayment("20000");
        savedContract.setType("Premium");
        savedContract.setStatus(false);

        Contract updatedContract = contractRepository.save(savedContract);

        // Then
        assertThat(updatedContract.getId()).isEqualTo(savedContract.getId());
        assertThat(updatedContract.getPayment()).isEqualTo(20000);
        assertThat(updatedContract.getType()).isEqualTo("Premium");
        assertThat(updatedContract.isStatus()).isFalse();

    }

    @Test
    void testDeleteContract() {
        // Given
        Contract contract = new Contract(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), "15000", true, "Delete");
        Contract savedContract = entityManager.persistAndFlush(contract);
        Long contractId = savedContract.getId();

        // When
        contractRepository.deleteById(contractId);

        // Then
        Optional<Contract> deletedContract = contractRepository.findById(contractId);
        assertThat(deletedContract).isNotPresent();
    }

    @Test
    void testCount() {
        // Given
        Contract contract1 = new Contract(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), "10000", true, "Type 1");
        Contract contract2 = new Contract(LocalDate.of(2024, 2, 1), LocalDate.of(2024, 11, 30), "20000", true, "Type 2");
        Contract contract3 = new Contract(LocalDate.of(2024, 3, 1), LocalDate.of(2024, 10, 31), "30000", false, "Type 3");
        entityManager.persistAndFlush(contract1);
        entityManager.persistAndFlush(contract2);
        entityManager.persistAndFlush(contract3);

        // When
        long count = contractRepository.count();

        // Then
        assertThat(count).isEqualTo(3);
    }

    @Test
    void testSaveContractWithPdfData() {
        // Given
        Contract contract = new Contract(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 12, 31), "50000", true, "Premium");
        byte[] pdfData = "Sample PDF Data".getBytes();
        contract.setPdfData(pdfData);


        // When
        Contract savedContract = contractRepository.save(contract);

        // Then
        assertThat(savedContract.getId()).isNotNull();
        assertThat(savedContract.getPdfData()).isEqualTo(pdfData);
    }

    @Test
    void testSaveContractWithEmptyConstructor() {
        // Given
        Contract contract = new Contract();
        contract.setStartDate(LocalDate.of(2024, 5, 1));
        contract.setEndDate(LocalDate.of(2024, 12, 31));
        contract.setPayment("35000");
        contract.setStatus(true);
        contract.setType("Custom");
        contract.setSponsorId(5L);


        // When
        Contract savedContract = contractRepository.save(contract);

        // Then
        assertThat(savedContract.getId()).isNotNull();
        assertThat(savedContract.getStartDate()).isEqualTo(LocalDate.of(2024, 5, 1));
        assertThat(savedContract.getPayment()).isEqualTo(35000);

    }

    @Test
    void testUpdateContractDates() {
        // Given
        Contract contract = new Contract(LocalDate.of(2024, 1, 1), LocalDate.of(2024, 6, 30), "10000", true, "Short Term");
        Contract savedContract = entityManager.persistAndFlush(contract);

        // When
        savedContract.setEndDate(LocalDate.of(2024, 12, 31));
        Contract updatedContract = contractRepository.save(savedContract);

        // Then
        assertThat(updatedContract.getEndDate()).isEqualTo(LocalDate.of(2024, 12, 31));
        assertThat(updatedContract.getStartDate()).isEqualTo(LocalDate.of(2024, 1, 1));
    }
}
