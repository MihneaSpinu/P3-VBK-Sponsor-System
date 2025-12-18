package p3project.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import p3project.classes.User;

public interface UserRepository extends JpaRepository<User, Long> {
	User findByName(String name);
}

