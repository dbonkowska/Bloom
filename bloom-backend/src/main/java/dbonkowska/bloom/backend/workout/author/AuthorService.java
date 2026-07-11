package dbonkowska.bloom.backend.workout.author;

import dbonkowska.bloom.backend.workout.WorkoutRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AuthorService {

    private final AuthorRepository authorRepository;
    private final WorkoutRepository workoutRepository;

    public AuthorService(AuthorRepository authorRepository, WorkoutRepository workoutRepository) {
        this.authorRepository = authorRepository;
        this.workoutRepository = workoutRepository;
    }

    public AuthorDto create(String name) {
        Author author = new Author();
        author.setName(name);
        return AuthorDto.from(authorRepository.save(author));
    }

    public List<AuthorDto> findAll() {
        return authorRepository.findAll().stream().map(AuthorDto::from).toList();
    }

    public AuthorDto findById(Long id) {
        return authorRepository.findById(id)
            .map(AuthorDto::from)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public AuthorDto update(Long id, String name) {
        Author author = authorRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        author.setName(name);
        return AuthorDto.from(authorRepository.save(author));
    }

    public void delete(Long id) {
        Author author = authorRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (workoutRepository.existsByAuthor(author)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Author has associated workouts");
        }
        authorRepository.deleteById(id);
    }
}