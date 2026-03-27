package dev.afduma.shiftplanner.shift.repository;

import dev.afduma.shiftplanner.shift.model.Shift;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ShiftRepository extends JpaRepository<Shift, UUID> {

  @Query(
      """
      select s
      from Shift s
      where (:teamId is null or s.team.id = :teamId)
        and (:userId is null or s.user.id = :userId)
        and (:fromAt is null or s.startAt >= :fromAt)
        and (:toAt is null or s.endAt <= :toAt)
      order by s.startAt asc
      """)
  List<Shift> search(
      @Param("teamId") UUID teamId,
      @Param("userId") UUID userId,
      @Param("fromAt") Instant fromAt,
      @Param("toAt") Instant toAt);

  @Query(
      """
            select count(s) > 0
            from Shift s
            where s.user.id = :userId
              and s.startAt < :endAt
              and s.endAt > :startAt
            """)
  boolean existsOverlappingShift(
      @Param("userId") UUID userId,
      @Param("startAt") Instant startAt,
      @Param("endAt") Instant endAt);

  @Query(
      """
            select count(s) > 0
            from Shift s
            where s.id <> :shiftId
              and s.user.id = :userId
              and s.startAt < :endAt
              and s.endAt > :startAt
            """)
  boolean existsOverlappingShiftExcludingId(
      @Param("shiftId") UUID shiftId,
      @Param("userId") UUID userId,
      @Param("startAt") Instant startAt,
      @Param("endAt") Instant endAt);
}
