package dbonkowska.bloom.backend.program;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PlannedSessionRepository extends JpaRepository<PlannedSession, Long> {
    void deleteAllByProgramCycle(ProgramCycle programCycle);
}