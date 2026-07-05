package dbonkowska.bloom.backend.workout;

import org.springframework.data.jpa.domain.Specification;

public class WorkoutSpecification {

    public static Specification<Workout> hasMuscleGroup(MuscleGroup muscleGroup) {
        return (root, query, cb) -> {
            query.distinct(true);
            var join = root.join("muscleGroups");
            return cb.equal(join, muscleGroup);
        };
    }

    public static Specification<Workout> byAuthorId(Long authorId) {
        return (root, query, cb) -> cb.equal(root.get("author").get("id"), authorId);
    }

    public static Specification<Workout> minDuration(int min) {
        return (root, query, cb) -> cb.greaterThanOrEqualTo(root.get("durationMinutes"), min);
    }

    public static Specification<Workout> maxDuration(int max) {
        return (root, query, cb) -> cb.lessThanOrEqualTo(root.get("durationMinutes"), max);
    }
}