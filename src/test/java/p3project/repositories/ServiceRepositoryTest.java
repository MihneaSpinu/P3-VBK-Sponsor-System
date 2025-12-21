package p3project.repositories;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import p3project.classes.Service;

@DataJpaTest
class ServiceRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ServiceRepository serviceRepository;

    @Test
    void testSaveService() {

        Service service = new Service("Banner", true, 100);

        Service savedService = serviceRepository.save(service);

        assertThat(savedService.getId()).isNotNull();
        assertThat(savedService.getType()).isEqualTo("Banner");
        assertThat(savedService.getActive()).isTrue();
        assertThat(savedService.getAmount()).isEqualTo(100);
    }

    @Test
    void testFindById() {

        Service service = new Service("Tickets", false, 50);
        Service persistedService = entityManager.persistAndFlush(service);

        Optional<Service> foundService = serviceRepository.findById(persistedService.getId());

        assertThat(foundService).isPresent();
        assertThat(foundService.get().getType()).isEqualTo("Tickets");
        assertThat(foundService.get().getActive()).isFalse();
        assertThat(foundService.get().getAmount()).isEqualTo(50);
    }

    @Test
    void testFindAll() {

        Service service1 = new Service("Banner", true, 100);
        Service service2 = new Service("Tickets", true, 200);
        Service service3 = new Service("Coupons", false, 150);
        entityManager.persistAndFlush(service1);
        entityManager.persistAndFlush(service2);
        entityManager.persistAndFlush(service3);

        List<Service> services = serviceRepository.findAll();

        assertThat(services).hasSize(3);
        assertThat(services).extracting(Service::getType)
            .containsExactlyInAnyOrder("Banner", "Tickets", "Coupons");
    }

    @Test
    void testUpdateService() {

        Service service = new Service("Banner", true, 100);
        Service savedService = entityManager.persistAndFlush(service);

        savedService.setType("Tickets");
        savedService.setActive(false);
        savedService.setAmount(250);
        Service updatedService = serviceRepository.save(savedService);

        assertThat(updatedService.getId()).isEqualTo(savedService.getId());
        assertThat(updatedService.getType()).isEqualTo("Tickets");
        assertThat(updatedService.getActive()).isFalse();
        assertThat(updatedService.getAmount()).isEqualTo(250);
    }

    @Test
    void testDeleteService() {

        Service service = new Service("Banner", true, 100);
        Service savedService = entityManager.persistAndFlush(service);
        Long serviceId = savedService.getId();

        serviceRepository.deleteById(serviceId);

        Optional<Service> deletedService = serviceRepository.findById(serviceId);
        assertThat(deletedService).isNotPresent();
    }

    @Test
    void testCount() {
 
        Service service1 = new Service("Banner", true, 100);
        Service service2 = new Service("Tickets", true, 200);
        entityManager.persistAndFlush(service1);
        entityManager.persistAndFlush(service2);

        long count = serviceRepository.count();

        assertThat(count).isEqualTo(2);
    }

    @Test
    void testSaveMultipleServicesWithSameType() {

        Service service1 = new Service("Banner", true, 100);
        Service service2 = new Service("Banner", true, 200);
        Service service3 = new Service("Banner", false, 150);

        serviceRepository.save(service1);
        serviceRepository.save(service2);
        serviceRepository.save(service3);
        List<Service> services = serviceRepository.findAll();

        assertThat(services).hasSize(3);
        assertThat(services).allMatch(s -> "Banner".equals(s.getType()));
    }

    @Test
    void testSaveServiceWithZeroAmount() {

        Service service = new Service("Coupons", true, 0);

        Service savedService = serviceRepository.save(service);

        assertThat(savedService.getId()).isNotNull();
        assertThat(savedService.getAmount()).isEqualTo(0);
    }

    @Test
    void testSaveServiceWithNegativeAmount() {

        Service service = new Service("Tickets", true, -50);

        Service savedService = serviceRepository.save(service);

        assertThat(savedService.getId()).isNotNull();
        assertThat(savedService.getAmount()).isEqualTo(-50);
    }
}