package dbonkowska.bloom.backend.workout;

import dbonkowska.bloom.backend.workout.author.Author;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface WorkoutRepository extends JpaRepository<Workout, Long>, JpaSpecificationExecutor<Workout> {

    boolean existsByAuthor(Author author);
}