package p3project.repositories;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import p3project.classes.Service;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
/*
@DataJpaTest
class ServiceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ServiceRepository serviceRepository;

    @Test
    void testSaveService() {
        // Given
        Service service = new Service(ServiceType.Banner, true, 100);

        // When
        Service savedService = serviceRepository.save(service);

        // Then
        assertThat(savedService.getId()).isNotNull();
        assertThat(savedService.getType()).isEqualTo(ServiceType.Banner);
        assertThat(savedService.getStatus()).isTrue();
        assertThat(savedService.getAmountOrDuration()).isEqualTo(100);
    }

    @Test
    void testFindById() {
        // Given
        Service service = new Service(ServiceType.Tickets, false, 50);
        Service persistedService = entityManager.persistAndFlush(service);

        // When
        Optional<Service> foundService = serviceRepository.findById(persistedService.getId());

        // Then
        assertThat(foundService).isPresent();
        assertThat(foundService.get().getType()).isEqualTo(ServiceType.Tickets);
        assertThat(foundService.get().getStatus()).isFalse();
        assertThat(foundService.get().getAmountOrDuration()).isEqualTo(50);
    }

    @Test
    void testFindAll() {
        // Given
        Service service1 = new Service(ServiceType.Banner, true, 100);
        Service service2 = new Service(ServiceType.Tickets, true, 200);
        Service service3 = new Service(ServiceType.Coupons, false, 150);
        entityManager.persistAndFlush(service1);
        entityManager.persistAndFlush(service2);
        entityManager.persistAndFlush(service3);

        // When
        List<Service> services = serviceRepository.findAll();

        // Then
        assertThat(services).hasSize(3);
        assertThat(services).extracting(Service::getType)
            .containsExactlyInAnyOrder(ServiceType.Banner, ServiceType.Tickets, ServiceType.Coupons);
    }

    @Test
    void testUpdateService() {
        // Given
        Service service = new Service(ServiceType.Banner, true, 100);
        Service savedService = entityManager.persistAndFlush(service);

        // When
        savedService.setType(ServiceType.Tickets);
        savedService.setStatus(false);
        savedService.setAmountOrDuration(250);
        Service updatedService = serviceRepository.save(savedService);

        // Then
        assertThat(updatedService.getId()).isEqualTo(savedService.getId());
        assertThat(updatedService.getType()).isEqualTo(ServiceType.Tickets);
        assertThat(updatedService.getStatus()).isFalse();
        assertThat(updatedService.getAmountOrDuration()).isEqualTo(250);
    }

    @Test
    void testDeleteService() {
        // Given
        Service service = new Service(ServiceType.Banner, true, 100);
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
        Service service1 = new Service(ServiceType.Banner, true, 100);
        Service service2 = new Service(ServiceType.Tickets, true, 200);
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
        Service service1 = new Service(ServiceType.Banner, true, 100);
        Service service2 = new Service(ServiceType.Banner, true, 200);
        Service service3 = new Service(ServiceType.Banner, false, 150);

        // When
        serviceRepository.save(service1);
        serviceRepository.save(service2);
        serviceRepository.save(service3);
        List<Service> services = serviceRepository.findAll();

        // Then
        assertThat(services).hasSize(3);
        assertThat(services).allMatch(s -> s.getType() == ServiceType.Banner);
    }

    @Test
    void testSaveServiceWithZeroAmount() {
        // Given
        Service service = new Service(ServiceType.Coupons, true, 0);

        // When
        Service savedService = serviceRepository.save(service);

        // Then
        assertThat(savedService.getId()).isNotNull();
        assertThat(savedService.getAmountOrDuration()).isEqualTo(0);
    }

    @Test
    void testSaveServiceWithNegativeAmount() {
        // Given
        Service service = new Service(ServiceType.Tickets, true, -50);

        // When
        Service savedService = serviceRepository.save(service);

        // Then
        assertThat(savedService.getId()).isNotNull();
        assertThat(savedService.getAmountOrDuration()).isEqualTo(-50);
    }
}
*/