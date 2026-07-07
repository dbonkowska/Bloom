package dbonkowska.bloom.backend.program.cycle;

import dbonkowska.bloom.backend.program.Program;
import dbonkowska.bloom.backend.program.ProgramDay;
import dbonkowska.bloom.backend.program.ProgramRepository;
import dbonkowska.bloom.backend.program.ProgramWorkout;
import dbonkowska.bloom.backend.program.session.PlannedSession;
import dbonkowska.bloom.backend.program.session.PlannedSessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ProgramCycleService {

    private final ProgramRepository programRepository;
    private final ProgramCycleRepository programCycleRepository;
    private final PlannedSessionRepository plannedSessionRepository;

    public ProgramCycleService(ProgramRepository programRepository,
                                ProgramCycleRepository programCycleRepository,
                                PlannedSessionRepository plannedSessionRepository) {
        this.programRepository = programRepository;
        this.programCycleRepository = programCycleRepository;
        this.plannedSessionRepository = plannedSessionRepository;
    }

    public ProgramCycleDto create(Long programId, ProgramCycleRequest request) {
        Program program = programRepository.findById(programId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (programCycleRepository.existsByProgramAndStartDate(program, request.startDate())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Cycle with this start date already exists");
        }

        ProgramCycle cycle = new ProgramCycle();
        cycle.setProgram(program);
        cycle.setStartDate(request.startDate());
        programCycleRepository.save(cycle);

        List<PlannedSession> sessions = new ArrayList<>();
        for (ProgramDay day : program.getDays()) {
            LocalDate date = request.startDate().plusDays(day.getDayNumber() - 1);
            for (ProgramWorkout pw : day.getWorkouts()) {
                PlannedSession session = new PlannedSession();
                session.setDate(date);
                session.setWorkout(pw.getWorkout());
                session.setProgramCycle(cycle);
                session.setProgramWorkout(pw);
                session.setCompleted(false);
                sessions.add(session);
            }
        }
        plannedSessionRepository.saveAll(sessions);

        return toDto(cycle);
    }

    public List<ProgramCycleDto> findAllByProgramId(Long programId) {
        Program program = programRepository.findById(programId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return programCycleRepository.findAllByProgram(program).stream()
            .map(this::toDto)
            .toList();
    }

    public ProgramCycleDto findById(Long id) {
        return programCycleRepository.findById(id)
            .map(this::toDto)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @Transactional
    public void delete(Long id) {
        ProgramCycle cycle = programCycleRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        plannedSessionRepository.deleteAllByProgramCycle(cycle);
        programCycleRepository.deleteById(id);
    }

    private ProgramCycleDto toDto(ProgramCycle cycle) {
        int numDays = cycle.getProgram().getDays().size();
        LocalDate endDate = numDays > 0
            ? cycle.getStartDate().plusDays(numDays - 1)
            : cycle.getStartDate();
        return new ProgramCycleDto(cycle.getId(), cycle.getProgram().getId(), cycle.getStartDate(), endDate);
    }
}