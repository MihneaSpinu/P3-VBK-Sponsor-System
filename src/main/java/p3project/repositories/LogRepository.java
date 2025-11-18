package p3project.repositories;

import org.springframework.data.repository.CrudRepository;

import p3project.classes.Eventlog;
import p3project.classes.Changelog;

public interface LogRepository extends CrudRepository<Eventlog, Integer>{

}
