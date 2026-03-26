package dev.afduma.shiftplanner.membership.repository;

import dev.afduma.shiftplanner.membership.model.TeamMembership;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamMembershipRepository extends JpaRepository<TeamMembership, UUID> {

  boolean existsByUserIdAndTeamId(UUID userId, UUID teamId);
}
