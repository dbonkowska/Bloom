package dbonkowska.bloom.backend.program.cycle;

import dbonkowska.bloom.backend.program.Program;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface ProgramCycleRepository extends JpaRepository<ProgramCycle, Long> {
    boolean existsByProgram(Program program);
    boolean existsByProgramAndStartDate(Program program, LocalDate startDate);
    List<ProgramCycle> findAllByProgram(Program program);
}