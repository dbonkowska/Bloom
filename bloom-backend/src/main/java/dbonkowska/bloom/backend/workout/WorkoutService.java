package dbonkowska.bloom.backend.workout;

import dbonkowska.bloom.backend.workout.author.Author;
import dbonkowska.bloom.backend.workout.author.AuthorDto;
import dbonkowska.bloom.backend.workout.author.AuthorRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class WorkoutService {

    private final WorkoutRepository workoutRepository;
    private final AuthorRepository authorRepository;

    public WorkoutService(WorkoutRepository workoutRepository, AuthorRepository authorRepository) {
        this.workoutRepository = workoutRepository;
        this.authorRepository = authorRepository;
    }

    public WorkoutDto create(WorkoutRequest request) {
        Author author = resolveAuthor(request.authorId());
        Workout workout = new Workout();
        applyRequest(workout, request, author);
        return toDto(workoutRepository.save(workout));
    }

    public List<WorkoutDto> findAll(MuscleGroup muscleGroup, Long authorId, Integer minDuration, Integer maxDuration) {
        Specification<Workout> spec = (_, _, cb) -> cb.conjunction();
        if (muscleGroup != null) spec = spec.and(WorkoutSpecification.hasMuscleGroup(muscleGroup));
        if (authorId != null) spec = spec.and(WorkoutSpecification.byAuthorId(authorId));
        if (minDuration != null) spec = spec.and(WorkoutSpecification.minDuration(minDuration));
        if (maxDuration != null) spec = spec.and(WorkoutSpecification.maxDuration(maxDuration));
        return workoutRepository.findAll(spec).stream().map(this::toDto).toList();
    }

    public WorkoutDto findById(Long id) {
        return workoutRepository.findById(id)
            .map(this::toDto)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    public WorkoutDto update(Long id, WorkoutRequest request) {
        Workout workout = workoutRepository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        Author author = resolveAuthor(request.authorId());
        applyRequest(workout, request, author);
        return toDto(workoutRepository.save(workout));
    }

    public void delete(Long id) {
        if (!workoutRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        workoutRepository.deleteById(id);
    }

    private Author resolveAuthor(Long authorId) {
        return authorRepository.findById(authorId)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Author not found"));
    }

    private void applyRequest(Workout workout, WorkoutRequest request, Author author) {
        workout.setName(request.name());
        workout.setYoutubeUrl(request.youtubeUrl());
        workout.setDurationMinutes(request.durationMinutes());
        workout.setMuscleGroups(request.muscleGroups());
        workout.setNotes(request.notes());
        workout.setAuthor(author);
    }

    private WorkoutDto toDto(Workout workout) {
        Author a = workout.getAuthor();
        return new WorkoutDto(
            workout.getId(),
            workout.getName(),
            workout.getYoutubeUrl(),
            workout.getDurationMinutes(),
            workout.getMuscleGroups(),
            workout.getNotes(),
            new AuthorDto(a.getId(), a.getName())
        );
    }
}