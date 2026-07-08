package dbonkowska.bloom.backend.program;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/programs")
public class ProgramController {

    private final ProgramService programService;

    public ProgramController(ProgramService programService) {
        this.programService = programService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProgramDto create(@Valid @RequestBody ProgramRequest request) {
        return programService.create(request);
    }

    @GetMapping
    public List<ProgramSummaryDto> list() {
        return programService.findAll();
    }

    @GetMapping("/{id}")
    public ProgramDto get(@PathVariable Long id) {
        return programService.findById(id);
    }

    @PutMapping("/{id}")
    public ProgramDto update(@PathVariable Long id, @Valid @RequestBody ProgramRequest request) {
        return programService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        programService.delete(id);
    }
}