package dbonkowska.bloom.backend.program.session;

import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/planned-sessions")
public class PlannedSessionController {

    private final PlannedSessionService plannedSessionService;

    public PlannedSessionController(PlannedSessionService plannedSessionService) {
        this.plannedSessionService = plannedSessionService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PlannedSessionDto create(@Valid @RequestBody PlannedSessionRequest request) {
        return plannedSessionService.create(request);
    }

    @GetMapping
    public List<PlannedSessionDto> list(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        return plannedSessionService.findAll(from, to);
    }

    @GetMapping("/{id}")
    public PlannedSessionDto get(@PathVariable Long id) {
        return plannedSessionService.findById(id);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        plannedSessionService.delete(id);
    }
}
