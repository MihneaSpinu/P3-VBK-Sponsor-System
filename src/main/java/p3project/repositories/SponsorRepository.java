package p3project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import p3project.classes.Sponsor;

public interface SponsorRepository extends JpaRepository<Sponsor, Long> {

}
