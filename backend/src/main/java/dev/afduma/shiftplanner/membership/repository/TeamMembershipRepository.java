package dev.afduma.shiftplanner.membership.repository;

import dev.afduma.shiftplanner.membership.model.TeamMembership;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamMembershipRepository extends JpaRepository<TeamMembership, UUID> {

  boolean existsByUser_IdAndTeam_Id(UUID userId, UUID teamId);

  @Query(
      """
      select membership.team.id
      from TeamMembership membership
      where membership.user.id = :userId
      """)
  List<UUID> findTeamIdsByUserId(@Param("userId") UUID userId);
}
