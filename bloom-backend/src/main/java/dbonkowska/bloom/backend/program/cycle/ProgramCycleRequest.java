package dbonkowska.bloom.backend.program.cycle;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record ProgramCycleRequest(@NotNull LocalDate startDate) {}