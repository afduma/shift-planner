package dev.afduma.shiftplanner.team.repository;

import dev.afduma.shiftplanner.team.model.Team;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TeamRepository extends JpaRepository<Team, UUID> {}
