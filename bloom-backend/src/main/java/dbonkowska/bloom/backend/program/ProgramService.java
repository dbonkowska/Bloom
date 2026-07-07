package dbonkowska.bloom.backend.program;

import dbonkowska.bloom.backend.program.cycle.ProgramCycleRepository;
import dbonkowska.bloom.backend.workout.Workout;
import dbonkowska.bloom.backend.workout.WorkoutRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProgramService {

    private final ProgramRepository programRepository;
    private final ProgramCycleRepository programCycleRepository;
    private final WorkoutRepository workoutRepository;

    public ProgramService(ProgramRepository programRepository, ProgramCycleRepository programCycleRepository, WorkoutRepository workoutRepository) {
        this.programRepository = programRepository;
        this.programCycleRepository = programCycleRepository;
        this.workoutRepository = workoutRepository;
    }

    public ProgramDto create(ProgramRequest request) {
        Program program = new Program();
        applyRequest(program, request);
        return toDto(programRepository.save(program));
    }

    public List<ProgramSummaryDto> findAll() {
        return programRepository.findAll().stream()
            .map(p -> new ProgramSummaryDto(p.getId(), p.getName(), p.getDescription(), p.getDays().size()))
            .toList();
    }

    public ProgramDto findById(Long id) {
        return programRepository.findById(id)
            .map(this::toDto)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public ProgramDto update(Long id, ProgramRequest request) {
        Program program = programRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        applyRequest(program, request);
        return toDto(programRepository.save(program));
    }

    public void delete(Long id) {
        Program program = programRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (programCycleRepository.existsByProgram(program)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Program has active cycles");
        }
        programRepository.deleteById(id);
    }

    private void applyRequest(Program program, ProgramRequest request) {
        program.setName(request.name());
        program.setDescription(request.description());

        List<ProgramDay> days = program.getDays();
        days.clear();
        List<ProgramDayRequest> dayRequests = request.days() != null ? request.days() : List.of();
        for (int i = 0; i < dayRequests.size(); i++) {
            ProgramDayRequest dayRequest = dayRequests.get(i);
            ProgramDay day = new ProgramDay();
            day.setDayNumber(i + 1);
            day.setProgram(program);
            day.setWorkouts(buildWorkouts(day, dayRequest));
            days.add(day);
        }
    }

    private List<ProgramWorkout> buildWorkouts(ProgramDay day, ProgramDayRequest dayRequest) {
        List<ProgramWorkout> workouts = new ArrayList<>();
        List<ProgramWorkoutRequest> requests = dayRequest.workouts() != null ? dayRequest.workouts() : List.of();
        for (int j = 0; j < requests.size(); j++) {
            Workout workout = workoutRepository.findById(requests.get(j).workoutId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Workout not found"));
            ProgramWorkout pw = new ProgramWorkout();
            pw.setProgramDay(day);
            pw.setWorkout(workout);
            pw.setOrder(j + 1);
            workouts.add(pw);
        }
        return workouts;
    }

    private ProgramDto toDto(Program program) {
        List<ProgramDayDto> dayDtos = program.getDays().stream()
            .map(this::toDayDto)
            .toList();
        return new ProgramDto(program.getId(), program.getName(), program.getDescription(), dayDtos);
    }

    private ProgramDayDto toDayDto(ProgramDay day) {
        List<ProgramWorkoutDto> workoutDtos = day.getWorkouts().stream()
            .map(pw -> new ProgramWorkoutDto(
                pw.getId(),
                new WorkoutSummaryDto(pw.getWorkout().getId(), pw.getWorkout().getName())
            ))
            .toList();
        return new ProgramDayDto(day.getId(), day.getDayNumber(), workoutDtos);
    }
}