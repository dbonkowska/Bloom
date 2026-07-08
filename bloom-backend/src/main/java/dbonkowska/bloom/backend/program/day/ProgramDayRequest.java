package dbonkowska.bloom.backend.program.day;

import dbonkowska.bloom.backend.program.workout.ProgramWorkoutRequest;
import java.util.List;

public record ProgramDayRequest(List<ProgramWorkoutRequest> workouts) {}