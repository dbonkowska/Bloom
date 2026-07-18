package dbonkowska.bloom.backend.workout;

import dbonkowska.bloom.backend.workout.author.AuthorDto;

import java.util.List;

public record WorkoutDto(
    Long id,
    String name,
    String youtubeUrl,
    Integer durationMinutes,
    List<MuscleGroup> muscleGroups,
    String notes,
    AuthorDto author
) {

    public static WorkoutDto from(Workout workout) {
        return new WorkoutDto(
            workout.getId(),
            workout.getName(),
            workout.getYoutubeUrl(),
            workout.getDurationMinutes(),
            workout.getMuscleGroups(),
            workout.getNotes(),
            AuthorDto.from(workout.getAuthor())
        );
    }
}