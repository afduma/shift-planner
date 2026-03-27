package dev.afduma.shiftplanner.membership.repository;

import dev.afduma.shiftplanner.membership.model.TeamMembership;
import dev.afduma.shiftplanner.membership.model.TeamRole;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TeamMembershipRepository extends JpaRepository<TeamMembership, UUID> {

  boolean existsByUser_IdAndTeam_Id(UUID userId, UUID teamId);

  Optional<TeamMembership> findByIdAndTeam_Id(UUID membershipId, UUID teamId);

  List<TeamMembership> findAllByTeam_IdOrderByCreatedAtAsc(UUID teamId);

  List<TeamMembership> findAllByUser_IdOrderByCreatedAtAsc(UUID userId);

  @Query(
      """
      select membership.team.id
      from TeamMembership membership
      where membership.user.id = :userId
      """)
  List<UUID> findTeamIdsByUserId(@Param("userId") UUID userId);

  @Query(
      """
      select membership.role
      from TeamMembership membership
      where membership.user.id = :userId
        and membership.team.id = :teamId
      """)
  Optional<TeamRole> findRoleByUserIdAndTeamId(
      @Param("userId") UUID userId, @Param("teamId") UUID teamId);
}
