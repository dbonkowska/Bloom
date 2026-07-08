package dbonkowska.bloom.backend.program.session;

import dbonkowska.bloom.backend.program.cycle.ProgramCycle;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PlannedSessionRepository extends JpaRepository<PlannedSession, Long> {
    void deleteAllByProgramCycle(ProgramCycle programCycle);
    List<PlannedSession> findByDateBetween(LocalDate from, LocalDate to);
    List<PlannedSession> findByDateGreaterThanEqual(LocalDate from);
    List<PlannedSession> findByDateLessThanEqual(LocalDate to);
}