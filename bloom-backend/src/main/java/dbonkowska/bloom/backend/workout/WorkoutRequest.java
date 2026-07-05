package dbonkowska.bloom.backend.workout;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record WorkoutRequest(
    @NotBlank String name,
    String youtubeUrl,
    Integer durationMinutes,
    List<MuscleGroup> muscleGroups,
    String notes,
    @NotNull Long authorId
) {}