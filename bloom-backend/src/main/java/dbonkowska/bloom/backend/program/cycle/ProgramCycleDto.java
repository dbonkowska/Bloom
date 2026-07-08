package dbonkowska.bloom.backend.program.cycle;

import java.time.LocalDate;

public record ProgramCycleDto(Long id, Long programId, String programName, LocalDate startDate, LocalDate endDate) {}