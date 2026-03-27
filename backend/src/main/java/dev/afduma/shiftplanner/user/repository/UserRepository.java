package dev.afduma.shiftplanner.user.repository;

import dev.afduma.shiftplanner.user.model.User;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, UUID> {

  boolean existsByEmailIgnoreCase(String email);

  Optional<User> findByEmailIgnoreCase(String email);

  List<User> findAllByOrderByCreatedAtAsc();
}
