package dbonkowska.bloom.backend.program.cycle;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
public class ProgramCycleController {

    private final ProgramCycleService programCycleService;

    public ProgramCycleController(ProgramCycleService programCycleService) {
        this.programCycleService = programCycleService;
    }

    @PostMapping("/api/programs/{programId}/cycles")
    @ResponseStatus(HttpStatus.CREATED)
    public ProgramCycleDto create(@PathVariable Long programId, @Valid @RequestBody ProgramCycleRequest request) {
        return programCycleService.create(programId, request);
    }

    @GetMapping("/api/programs/{programId}/cycles")
    public List<ProgramCycleDto> list(@PathVariable Long programId) {
        return programCycleService.findAllByProgramId(programId);
    }

    @GetMapping("/api/program-cycles/{id}")
    public ProgramCycleDto get(@PathVariable Long id) {
        return programCycleService.findById(id);
    }

    @DeleteMapping("/api/program-cycles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        programCycleService.delete(id);
    }
}