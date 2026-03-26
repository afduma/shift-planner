package dev.afduma.shiftplanner.shift.repository;

import dev.afduma.shiftplanner.shift.model.Shift;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShiftRepository extends JpaRepository<Shift, UUID> {

  @Query(
      """
            select count(s) > 0
            from Shift s
            where s.user.id = :userId
              and s.shiftDate = :shiftDate
              and s.startTime < :endTime
              and s.endTime > :startTime
            """)
  boolean existsOverlappingShift(
      @Param("userId") UUID userId,
      @Param("shiftDate") LocalDate shiftDate,
      @Param("startTime") LocalTime startTime,
      @Param("endTime") LocalTime endTime);
}
