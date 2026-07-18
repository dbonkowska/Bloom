package dbonkowska.bloom.backend.program.session;

import dbonkowska.bloom.backend.program.WorkoutSummaryDto;

import java.time.LocalDate;

public record PlannedSessionDto(
    Long id,
    LocalDate date,
    WorkoutSummaryDto workout,
    Long programCycleId,
    Long programWorkoutId,
    boolean completed
) {

    public static PlannedSessionDto from(PlannedSession session) {
        return new PlannedSessionDto(
            session.getId(),
            session.getDate(),
            WorkoutSummaryDto.from(session.getWorkout()),
            session.getProgramCycle() != null ? session.getProgramCycle().getId() : null,
            session.getProgramWorkout() != null ? session.getProgramWorkout().getId() : null,
            session.isCompleted()
        );
    }
}