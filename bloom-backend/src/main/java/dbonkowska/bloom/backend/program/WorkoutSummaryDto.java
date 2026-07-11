package dbonkowska.bloom.backend.program;

import dbonkowska.bloom.backend.workout.Workout;

public record WorkoutSummaryDto(Long id, String name) {

    public static WorkoutSummaryDto from(Workout workout) {
        return new WorkoutSummaryDto(workout.getId(), workout.getName());
    }
}