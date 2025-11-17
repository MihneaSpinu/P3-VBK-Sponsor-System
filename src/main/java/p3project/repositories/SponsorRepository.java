package p3project.repositories;

import org.springframework.data.repository.CrudRepository;

import p3project.classes.Sponsor;

public interface SponsorRepository extends CrudRepository<Sponsor, String> {
    // Spring will auto-implement this

}
