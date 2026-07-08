package dbonkowska.bloom.backend.program.workout;

import dbonkowska.bloom.backend.program.WorkoutSummaryDto;

public record ProgramWorkoutDto(Long id, WorkoutSummaryDto workout, Integer order) {}