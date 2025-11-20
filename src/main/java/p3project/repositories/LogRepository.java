package p3project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import p3project.classes.Eventlog;
import p3project.classes.Changelog;

public interface LogRepository extends JpaRepository<Eventlog, Integer>{

}
