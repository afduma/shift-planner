package dev.afduma.shiftplanner.team.repository;

import dev.afduma.shiftplanner.team.model.Team;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, UUID> {

  List<Team> findAllByOrderByNameAsc();

  List<Team> findAllByIdInOrderByNameAsc(Collection<UUID> ids);
}
