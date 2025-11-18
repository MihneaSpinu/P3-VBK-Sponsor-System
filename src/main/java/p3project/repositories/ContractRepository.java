package p3project.repositories;

import org.springframework.data.repository.CrudRepository;

import p3project.classes.Contract;

public interface ContractRepository extends CrudRepository<Contract, Long> {
    // Spring will auto-implement this

}
