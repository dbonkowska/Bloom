package dbonkowska.bloom.backend.workout.author;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/authors")
public class AuthorController {

    private final AuthorService service;

    public AuthorController(AuthorService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AuthorDto create(@RequestBody @Valid AuthorRequest request) {
        return service.create(request.name());
    }

    @GetMapping
    public List<AuthorDto> list() {
        return service.findAll();
    }

    @GetMapping("/{id}")
    public AuthorDto getById(@PathVariable Long id) {
        return service.findById(id);
    }

    @PutMapping("/{id}")
    public AuthorDto update(@PathVariable Long id, @RequestBody @Valid AuthorRequest request) {
        return service.update(id, request.name());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        service.delete(id);
    }
}