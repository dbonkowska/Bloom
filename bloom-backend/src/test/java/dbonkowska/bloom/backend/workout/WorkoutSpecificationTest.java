package dbonkowska.bloom.backend.workout;

import dbonkowska.bloom.backend.workout.author.Author;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureDataSourceInitialization;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ImportTestcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureDataSourceInitialization
class WorkoutSpecificationTest {

    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private TestEntityManager em;

    @Autowired
    private WorkoutRepository workoutRepository;

    private Author a1;
    private Author a2;

    @BeforeEach
    void seed() {
        a1 = new Author();
        a1.setName("CG");
        em.persist(a1);

        a2 = new Author();
        a2.setName("Other");
        em.persist(a2);

        em.persist(workout("EPIC Day 1", List.of(MuscleGroup.LEGS, MuscleGroup.GLUTES), 30, a1));
        em.persist(workout("EPIC Day 2", List.of(MuscleGroup.BACK), 45, a1));
        em.persist(workout("Quick Abs", List.of(MuscleGroup.ABS), 20, a1));
        em.persist(workout("Yoga Flow", List.of(MuscleGroup.TOTAL_BODY), 60, a2));
        em.flush();
    }

    @Test
    void hasMuscleGroup_LEGS_returnsOnlyEpicDay1() {
        List<Workout> result = workoutRepository.findAll(WorkoutSpecification.hasMuscleGroup(MuscleGroup.LEGS));
        assertThat(result).extracting(Workout::getName).containsExactlyInAnyOrder("EPIC Day 1");
    }

    @Test
    void hasMuscleGroup_ABS_returnsOnlyQuickAbs() {
        List<Workout> result = workoutRepository.findAll(WorkoutSpecification.hasMuscleGroup(MuscleGroup.ABS));
        assertThat(result).extracting(Workout::getName).containsExactlyInAnyOrder("Quick Abs");
    }

    @Test
    void byAuthorId_a1_returnsThreeWorkouts() {
        List<Workout> result = workoutRepository.findAll(WorkoutSpecification.byAuthorId(a1.getId()));
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Workout::getName)
            .containsExactlyInAnyOrder("EPIC Day 1", "EPIC Day 2", "Quick Abs");
    }

    @Test
    void minDuration_30_returnsThreeWorkouts() {
        List<Workout> result = workoutRepository.findAll(WorkoutSpecification.minDuration(30));
        assertThat(result).hasSize(3);
        assertThat(result).extracting(Workout::getDurationMinutes)
            .allMatch(d -> d >= 30);
    }

    @Test
    void maxDuration_30_returnsTwoWorkouts() {
        List<Workout> result = workoutRepository.findAll(WorkoutSpecification.maxDuration(30));
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Workout::getDurationMinutes)
            .allMatch(d -> d <= 30);
    }

    @Test
    void minAndMaxDuration_30to45_returnsTwoWorkouts() {
        List<Workout> result = workoutRepository.findAll(
            WorkoutSpecification.minDuration(30).and(WorkoutSpecification.maxDuration(45))
        );
        assertThat(result).hasSize(2);
        assertThat(result).extracting(Workout::getDurationMinutes)
            .containsExactlyInAnyOrder(30, 45);
    }

    @Test
    void combinedSpec_legsAndA1AndMin25_returnsOnlyEpicDay1() {
        List<Workout> result = workoutRepository.findAll(
            WorkoutSpecification.hasMuscleGroup(MuscleGroup.LEGS)
                .and(WorkoutSpecification.byAuthorId(a1.getId()))
                .and(WorkoutSpecification.minDuration(25))
        );
        assertThat(result).extracting(Workout::getName).containsExactlyInAnyOrder("EPIC Day 1");
    }

    @Test
    void combinedSpec_backMuscleGroupAndMaxDuration50_returnsOnlyEpicDay2() {
        List<Workout> result = workoutRepository.findAll(
            WorkoutSpecification.hasMuscleGroup(MuscleGroup.BACK)
                .and(WorkoutSpecification.maxDuration(50))
        );
        assertThat(result).extracting(Workout::getName).containsExactlyInAnyOrder("EPIC Day 2");
    }

    private Workout workout(String name, List<MuscleGroup> muscleGroups, int durationMinutes, Author author) {
        Workout w = new Workout();
        w.setName(name);
        w.setMuscleGroups(muscleGroups);
        w.setDurationMinutes(durationMinutes);
        w.setAuthor(author);
        return w;
    }
}