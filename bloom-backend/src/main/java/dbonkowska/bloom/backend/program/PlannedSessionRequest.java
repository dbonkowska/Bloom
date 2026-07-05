package dbonkowska.bloom.backend.program;

import java.time.LocalDate;

public record PlannedSessionRequest(LocalDate date, Long workoutId, Long programCycleId, Long programWorkoutId) {}