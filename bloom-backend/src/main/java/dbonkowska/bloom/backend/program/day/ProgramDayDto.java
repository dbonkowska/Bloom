package dbonkowska.bloom.backend.program.day;

import dbonkowska.bloom.backend.program.workout.ProgramWorkoutDto;
import java.util.List;

public record ProgramDayDto(Long id, Integer dayNumber, List<ProgramWorkoutDto> workouts) {

    public static ProgramDayDto from(ProgramDay day) {
        return new ProgramDayDto(
            day.getId(),
            day.getDayNumber(),
            day.getWorkouts().stream().map(ProgramWorkoutDto::from).toList()
        );
    }
}