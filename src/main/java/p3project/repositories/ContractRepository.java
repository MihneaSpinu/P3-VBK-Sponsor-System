package p3project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import p3project.classes.Contract;

public interface ContractRepository extends JpaRepository<Contract, Long> {

}
