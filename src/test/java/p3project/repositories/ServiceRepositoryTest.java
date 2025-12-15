package p3project.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import p3project.classes.Service;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ServiceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ServiceRepository serviceRepository;

    @Test
    void testSaveService() {
        // Given
        Service service = new Service("Banner", true, 100);

        // When
        Service savedService = serviceRepository.save(service);

        // Then
        assertThat(savedService.getId()).isNotNull();
        assertThat(savedService.getType()).isEqualTo("Banner");
        assertThat(savedService.getActive()).isTrue();
        assertThat(savedService.getAmountOrDivision()).isEqualTo(100);
    }

    @Test
    void testFindById() {
        // Given
        Service service = new Service("Tickets", false, 50);
        Service persistedService = entityManager.persistAndFlush(service);

        // When
        Optional<Service> foundService = serviceRepository.findById(persistedService.getId());

        // Then
        assertThat(foundService).isPresent();
        assertThat(foundService.get().getType()).isEqualTo("Tickets");
        assertThat(foundService.get().getActive()).isFalse();
        assertThat(foundService.get().getAmountOrDivision()).isEqualTo(50);
    }

    @Test
    void testFindAll() {
        // Given
        Service service1 = new Service("Banner", true, 100);
        Service service2 = new Service("Tickets", true, 200);
        Service service3 = new Service("Coupons", false, 150);
        entityManager.persistAndFlush(service1);
        entityManager.persistAndFlush(service2);
        entityManager.persistAndFlush(service3);

        // When
        List<Service> services = serviceRepository.findAll();

        // Then
        assertThat(services).hasSize(3);
        assertThat(services).extracting(Service::getType)
            .containsExactlyInAnyOrder("Banner", "Tickets", "Coupons");
    }

    @Test
    void testUpdateService() {
        // Given
        Service service = new Service("Banner", true, 100);
        Service savedService = entityManager.persistAndFlush(service);

        // When
        savedService.setType("Tickets");
        savedService.setActive(false);
        savedService.setAmountOrDivision(250);
        Service updatedService = serviceRepository.save(savedService);

        // Then
        assertThat(updatedService.getId()).isEqualTo(savedService.getId());
        assertThat(updatedService.getType()).isEqualTo("Tickets");
        assertThat(updatedService.getActive()).isFalse();
        assertThat(updatedService.getAmountOrDivision()).isEqualTo(250);
    }

    @Test
    void testDeleteService() {
        // Given
        Service service = new Service("Banner", true, 100);
        Service savedService = entityManager.persistAndFlush(service);
        Long serviceId = savedService.getId();

        // When
        serviceRepository.deleteById(serviceId);

        // Then
        Optional<Service> deletedService = serviceRepository.findById(serviceId);
        assertThat(deletedService).isNotPresent();
    }

    @Test
    void testCount() {
        // Given
        Service service1 = new Service("Banner", true, 100);
        Service service2 = new Service("Tickets", true, 200);
        entityManager.persistAndFlush(service1);
        entityManager.persistAndFlush(service2);

        // When
        long count = serviceRepository.count();

        // Then
        assertThat(count).isEqualTo(2);
    }

    @Test
    void testSaveMultipleServicesWithSameType() {
        // Given
        Service service1 = new Service("Banner", true, 100);
        Service service2 = new Service("Banner", true, 200);
        Service service3 = new Service("Banner", false, 150);

        // When
        serviceRepository.save(service1);
        serviceRepository.save(service2);
        serviceRepository.save(service3);
        List<Service> services = serviceRepository.findAll();

        // Then
        assertThat(services).hasSize(3);
        assertThat(services).allMatch(s -> "Banner".equals(s.getType()));
    }

    @Test
    void testSaveServiceWithZeroAmount() {
        // Given
        Service service = new Service("Coupons", true, 0);

        // When
        Service savedService = serviceRepository.save(service);

        // Then
        assertThat(savedService.getId()).isNotNull();
        assertThat(savedService.getAmountOrDivision()).isEqualTo(0);
    }

    @Test
    void testSaveServiceWithNegativeAmount() {
        // Given
        Service service = new Service("Tickets", true, -50);

        // When
        Service savedService = serviceRepository.save(service);

        // Then
        assertThat(savedService.getId()).isNotNull();
        assertThat(savedService.getAmountOrDivision()).isEqualTo(-50);
    }
}