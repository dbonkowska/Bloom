package dbonkowska.bloom.backend.program;

import java.util.List;

public record ProgramDayRequest(List<ProgramWorkoutRequest> workouts) {}