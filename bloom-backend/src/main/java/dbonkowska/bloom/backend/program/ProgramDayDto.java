package dbonkowska.bloom.backend.program;

import java.util.List;

public record ProgramDayDto(Long id, Integer dayNumber, List<ProgramWorkoutDto> workouts) {}