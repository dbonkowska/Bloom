package dbonkowska.bloom.backend.program;

import java.time.LocalDate;

public record PlannedSessionDto(
    Long id,
    LocalDate date,
    WorkoutSummaryDto workout,
    Long programCycleId,
    Long programWorkoutId,
    boolean completed
) {}