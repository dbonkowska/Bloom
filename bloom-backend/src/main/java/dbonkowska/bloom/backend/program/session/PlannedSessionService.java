package dbonkowska.bloom.backend.program.session;

import dbonkowska.bloom.backend.program.WorkoutSummaryDto;
import dbonkowska.bloom.backend.program.cycle.ProgramCycle;
import dbonkowska.bloom.backend.program.cycle.ProgramCycleRepository;
import dbonkowska.bloom.backend.workout.Workout;
import dbonkowska.bloom.backend.workout.WorkoutRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
public class PlannedSessionService {

    private final PlannedSessionRepository plannedSessionRepository;
    private final WorkoutRepository workoutRepository;
    private final ProgramCycleRepository programCycleRepository;

    public PlannedSessionService(PlannedSessionRepository plannedSessionRepository,
                                  WorkoutRepository workoutRepository,
                                  ProgramCycleRepository programCycleRepository) {
        this.plannedSessionRepository = plannedSessionRepository;
        this.workoutRepository = workoutRepository;
        this.programCycleRepository = programCycleRepository;
    }

    public PlannedSessionDto create(PlannedSessionRequest request) {
        Workout workout = workoutRepository.findById(request.workoutId())
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Workout not found"));

        ProgramCycle programCycle = null;
        if (request.programCycleId() != null) {
            programCycle = programCycleRepository.findById(request.programCycleId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "ProgramCycle not found"));
        }

        PlannedSession session = new PlannedSession();
        session.setDate(request.date());
        session.setWorkout(workout);
        session.setProgramCycle(programCycle);
        session.setCompleted(false);

        return toDto(plannedSessionRepository.save(session));
    }

    public List<PlannedSessionDto> findAll(LocalDate from, LocalDate to) {
        List<PlannedSession> sessions;
        if (from != null && to != null) {
            sessions = plannedSessionRepository.findByDateBetween(from, to);
        } else if (from != null) {
            sessions = plannedSessionRepository.findByDateGreaterThanEqual(from);
        } else if (to != null) {
            sessions = plannedSessionRepository.findByDateLessThanEqual(to);
        } else {
            sessions = plannedSessionRepository.findAll();
        }
        return sessions.stream().map(this::toDto).toList();
    }

    public PlannedSessionDto findById(Long id) {
        return plannedSessionRepository.findById(id)
            .map(this::toDto)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public void delete(Long id) {
        if (!plannedSessionRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        plannedSessionRepository.deleteById(id);
    }

    public PlannedSessionDto reschedule(Long id, PlannedSessionRescheduleRequest request) {
        PlannedSession session = plannedSessionRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        session.setDate(request.date());
        return toDto(plannedSessionRepository.save(session));
    }

    public PlannedSessionDto complete(Long id) {
        PlannedSession session = plannedSessionRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        session.setCompleted(true);
        return toDto(plannedSessionRepository.save(session));
    }

    public PlannedSessionDto incomplete(Long id) {
        PlannedSession session = plannedSessionRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        session.setCompleted(false);
        return toDto(plannedSessionRepository.save(session));
    }

    private PlannedSessionDto toDto(PlannedSession session) {
        return new PlannedSessionDto(
            session.getId(),
            session.getDate(),
            new WorkoutSummaryDto(session.getWorkout().getId(), session.getWorkout().getName()),
            session.getProgramCycle() != null ? session.getProgramCycle().getId() : null,
            session.getProgramWorkout() != null ? session.getProgramWorkout().getId() : null,
            session.isCompleted()
        );
    }
}