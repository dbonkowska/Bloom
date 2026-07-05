package dbonkowska.bloom.backend.workout;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/workouts")
public class WorkoutController {

    private final WorkoutService service;

    public WorkoutController(WorkoutService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WorkoutDto create(@RequestBody @Valid WorkoutRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<WorkoutDto> list(
        @RequestParam(required = false) MuscleGroup muscleGroup,
        @RequestParam(required = false) Long authorId,
        @RequestParam(required = false) Integer minDuration,
        @RequestParam(required = false) Integer maxDuration
    ) {
        return service.findAll(muscleGroup, authorId, minDuration, maxDuration);
    }

    @GetMapping("/{id}")
    public WorkoutDto getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public WorkoutDto update(@PathVariable Long id, @RequestBody @Valid WorkoutRequest request) {
        return service.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}