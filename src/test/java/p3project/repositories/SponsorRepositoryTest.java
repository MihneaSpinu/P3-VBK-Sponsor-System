package p3project.repositories;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import p3project.classes.Sponsor;

@DataJpaTest
class SponsorRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private SponsorRepository sponsorRepository;

    @Test
    void testSaveSponsor() {

        Sponsor sponsor = new Sponsor(
            "Tech Corp",
            "John Smith",
            "john@techcorp.com",
            "12345678",
            "CVR123456",
            true,
            "Premium sponsor"
        );

        Sponsor savedSponsor = sponsorRepository.save(sponsor);

        assertThat(savedSponsor.getId()).isNotNull();
        assertThat(savedSponsor.getName()).isEqualTo("Tech Corp");
        assertThat(savedSponsor.getContactPerson()).isEqualTo("John Smith");
        assertThat(savedSponsor.getEmail()).isEqualTo("john@techcorp.com");
        assertThat(savedSponsor.getActive()).isTrue();
    }

    @Test
    void testFindById() {

        Sponsor sponsor = new Sponsor(
            "Sports Inc",
            "Jane Doe",
            "jane@sportsinc.com",
            "87654321",
            "CVR654321",
            false,
            "Inactive sponsor"
        );
        Sponsor persistedSponsor = entityManager.persistAndFlush(sponsor);

        Optional<Sponsor> foundSponsor = sponsorRepository.findById(persistedSponsor.getId());

        assertThat(foundSponsor).isPresent();
        assertThat(foundSponsor.get().getName()).isEqualTo("Sports Inc");
        assertThat(foundSponsor.get().getContactPerson()).isEqualTo("Jane Doe");
        assertThat(foundSponsor.get().getActive()).isFalse();
    }

    @Test
    void testFindAll() {

        Sponsor sponsor1 = new Sponsor("Company A", "Contact A", "a@company.com", "11111111", "CVRA", true, "Comment A");
        Sponsor sponsor2 = new Sponsor("Company B", "Contact B", "b@company.com", "22222222", "CVRB", true, "Comment B");
        entityManager.persistAndFlush(sponsor1);
        entityManager.persistAndFlush(sponsor2);

        List<Sponsor> sponsors = sponsorRepository.findAll();

        assertThat(sponsors).hasSize(2);
        assertThat(sponsors).extracting(Sponsor::getName).containsExactlyInAnyOrder("Company A", "Company B");
    }

    @Test
    void testUpdateSponsor() {

        Sponsor sponsor = new Sponsor("Original Corp", "Original Contact", "original@corp.com", "99999999", "CVRORG", true, "Original");
        Sponsor savedSponsor = entityManager.persistAndFlush(sponsor);

        savedSponsor.setName("Updated Corp");
        savedSponsor.setContactPerson("Updated Contact");
        savedSponsor.setActive(false);
        Sponsor updatedSponsor = sponsorRepository.save(savedSponsor);

        assertThat(updatedSponsor.getId()).isEqualTo(savedSponsor.getId());
        assertThat(updatedSponsor.getName()).isEqualTo("Updated Corp");
        assertThat(updatedSponsor.getContactPerson()).isEqualTo("Updated Contact");
        assertThat(updatedSponsor.getActive()).isFalse();
    }

    @Test
    void testDeleteSponsor() {

        Sponsor sponsor = new Sponsor("Delete Corp", "Delete Contact", "delete@corp.com", "00000000", "CVRDEL", true, "To be deleted");
        Sponsor savedSponsor = entityManager.persistAndFlush(sponsor);
        Long sponsorId = savedSponsor.getId();

        sponsorRepository.deleteById(sponsorId);

        Optional<Sponsor> deletedSponsor = sponsorRepository.findById(sponsorId);
        assertThat(deletedSponsor).isNotPresent();
    }

    @Test
    void testCount() {

        Sponsor sponsor1 = new Sponsor("Count Corp 1", "Contact 1", "count1@corp.com", "11111111", "CVR1", true, "Count 1");
        Sponsor sponsor2 = new Sponsor("Count Corp 2", "Contact 2", "count2@corp.com", "22222222", "CVR2", true, "Count 2");
        Sponsor sponsor3 = new Sponsor("Count Corp 3", "Contact 3", "count3@corp.com", "33333333", "CVR3", false, "Count 3");
        entityManager.persistAndFlush(sponsor1);
        entityManager.persistAndFlush(sponsor2);
        entityManager.persistAndFlush(sponsor3);

        long count = sponsorRepository.count();

        assertThat(count).isEqualTo(3);
    }

    @Test
    void testSaveSponsorWithEmptyConstructor() {

        Sponsor sponsor = new Sponsor();
        sponsor.setName("Empty Constructor Corp");
        sponsor.setContactPerson("Empty Contact");
        sponsor.setEmail("empty@corp.com");
        sponsor.setPhoneNumber("55555555");
        sponsor.setCvrNumber("CVREMP");
        sponsor.setActive(true);
        sponsor.setComments("Created with empty constructor");

        Sponsor savedSponsor = sponsorRepository.save(sponsor);

        assertThat(savedSponsor.getId()).isNotNull();
        assertThat(savedSponsor.getName()).isEqualTo("Empty Constructor Corp");
        assertThat(savedSponsor.getContactPerson()).isEqualTo("Empty Contact");
    }
}
