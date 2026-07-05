package dbonkowska.bloom.backend.program;

import java.time.LocalDate;

public record ProgramCycleDto(Long id, Long programId, LocalDate startDate, LocalDate endDate) {}