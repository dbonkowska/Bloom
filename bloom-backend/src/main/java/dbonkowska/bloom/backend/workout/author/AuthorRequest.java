package dbonkowska.bloom.backend.workout.author;

import jakarta.validation.constraints.NotBlank;

public record AuthorRequest(@NotBlank String name) {}