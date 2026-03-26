package dev.afduma.shiftplanner.auth.repository;

import dev.afduma.shiftplanner.auth.model.IdentityProvider;
import dev.afduma.shiftplanner.auth.model.UserIdentity;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserIdentityRepository extends JpaRepository<UserIdentity, UUID> {

  Optional<UserIdentity> findByProviderAndSubject(IdentityProvider provider, String subject);
}
