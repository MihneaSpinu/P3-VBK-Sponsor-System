package p3project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import p3project.classes.Service;

public interface ServiceRepository extends JpaRepository<Service, Long> {

}
