package dbonkowska.bloom.backend.program;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProgramCycleRepository extends JpaRepository<ProgramCycle, Long> {
    boolean existsByProgram(Program program);
    boolean existsByProgramAndStartDate(Program program, java.time.LocalDate startDate);
    java.util.List<ProgramCycle> findAllByProgram(Program program);
}