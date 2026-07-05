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
) {}