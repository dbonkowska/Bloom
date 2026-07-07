package dbonkowska.bloom.backend.program.session;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record PlannedSessionRequest(
    @NotNull LocalDate date,
    @NotNull Long workoutId,
    Long programCycleId,
    Long programWorkoutId
) {}