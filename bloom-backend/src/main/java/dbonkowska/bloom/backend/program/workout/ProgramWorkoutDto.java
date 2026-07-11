package dbonkowska.bloom.backend.program.workout;

import dbonkowska.bloom.backend.program.WorkoutSummaryDto;

public record ProgramWorkoutDto(Long id, WorkoutSummaryDto workout, Integer order) {

    public static ProgramWorkoutDto from(ProgramWorkout programWorkout) {
        return new ProgramWorkoutDto(
            programWorkout.getId(),
            WorkoutSummaryDto.from(programWorkout.getWorkout()),
            programWorkout.getOrder()
        );
    }
}