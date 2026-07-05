package dbonkowska.bloom.backend.workout;

import dbonkowska.bloom.backend.workout.author.Author;
import dbonkowska.bloom.backend.workout.author.AuthorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ImportTestcontainers
class WorkoutIntegrationTest {

    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired MockMvc mvc;
    @Autowired WorkoutRepository workoutRepository;
    @Autowired AuthorRepository authorRepository;

    private Author defaultAuthor;

    @BeforeEach
    void setUp() {
        workoutRepository.deleteAll();
        authorRepository.deleteAll();
        defaultAuthor = authorRepository.save(author("Caroline Girvan"));
    }

    // --- POST ---

    @Test
    void createWorkout_allFields_returns201WithDto() throws Exception {
        mvc.perform(post("/api/workouts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "EPIC Day 1",
                        "youtubeUrl": "https://yt.com/watch?v=abc",
                        "durationMinutes": 30,
                        "muscleGroups": ["LEGS", "GLUTES"],
                        "notes": "Hard session",
                        "authorId": %d
                    }
                    """.formatted(defaultAuthor.getId())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.name").value("EPIC Day 1"))
            .andExpect(jsonPath("$.youtubeUrl").value("https://yt.com/watch?v=abc"))
            .andExpect(jsonPath("$.durationMinutes").value(30))
            .andExpect(jsonPath("$.muscleGroups", hasSize(2)))
            .andExpect(jsonPath("$.author.id").value(defaultAuthor.getId()))
            .andExpect(jsonPath("$.author.name").value("Caroline Girvan"));
    }

    @Test
    void createWorkout_requiredFieldsOnly_returns201() throws Exception {
        mvc.perform(post("/api/workouts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "Quick Abs", "authorId": %d}
                    """.formatted(defaultAuthor.getId())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.name").value("Quick Abs"))
            .andExpect(jsonPath("$.author.id").value(defaultAuthor.getId()));
    }

    @Test
    void createWorkout_missingName_returns400() throws Exception {
        mvc.perform(post("/api/workouts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"authorId": %d}
                    """.formatted(defaultAuthor.getId())))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createWorkout_missingAuthorId_returns400() throws Exception {
        mvc.perform(post("/api/workouts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "EPIC Day 1"}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createWorkout_unknownAuthorId_returns400() throws Exception {
        mvc.perform(post("/api/workouts")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "EPIC Day 1", "authorId": 999}
                    """))
            .andExpect(status().isBadRequest());
    }

    // --- GET list ---

    @Test
    void listWorkouts_returnsAll() throws Exception {
        workoutRepository.saveAll(List.of(
            workout("EPIC Day 1", defaultAuthor),
            workout("EPIC Day 2", defaultAuthor)
        ));

        mvc.perform(get("/api/workouts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void listWorkouts_emptyDb_returnsEmptyArray() throws Exception {
        mvc.perform(get("/api/workouts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    // --- GET by id ---

    @Test
    void getWorkout_existingId_returns200WithDto() throws Exception {
        Workout saved = workoutRepository.save(workout("EPIC Day 1", defaultAuthor));

        mvc.perform(get("/api/workouts/" + saved.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(saved.getId()))
            .andExpect(jsonPath("$.author.name").value("Caroline Girvan"));
    }

    @Test
    void getWorkout_unknownId_returns404() throws Exception {
        mvc.perform(get("/api/workouts/999"))
            .andExpect(status().isNotFound());
    }

    // --- PUT ---

    @Test
    void updateWorkout_existingId_returns200WithUpdatedDto() throws Exception {
        Workout saved = workoutRepository.save(workout("EPIC Day 1", defaultAuthor));

        mvc.perform(put("/api/workouts/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "EPIC Day 1 Updated", "durationMinutes": 45, "authorId": %d}
                    """.formatted(defaultAuthor.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("EPIC Day 1 Updated"))
            .andExpect(jsonPath("$.durationMinutes").value(45));
    }

    @Test
    void updateWorkout_unknownId_returns404() throws Exception {
        mvc.perform(put("/api/workouts/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "Updated", "authorId": %d}
                    """.formatted(defaultAuthor.getId())))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateWorkout_missingName_returns400() throws Exception {
        Workout saved = workoutRepository.save(workout("EPIC Day 1", defaultAuthor));

        mvc.perform(put("/api/workouts/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"authorId": %d}
                    """.formatted(defaultAuthor.getId())))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateWorkout_missingAuthorId_returns400() throws Exception {
        Workout saved = workoutRepository.save(workout("EPIC Day 1", defaultAuthor));

        mvc.perform(put("/api/workouts/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "EPIC Day 1"}
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateWorkout_unknownAuthorId_returns400() throws Exception {
        Workout saved = workoutRepository.save(workout("EPIC Day 1", defaultAuthor));

        mvc.perform(put("/api/workouts/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "EPIC Day 1", "authorId": 999}
                    """))
            .andExpect(status().isBadRequest());
    }

    // --- DELETE ---

    @Test
    void deleteWorkout_existingId_returns204() throws Exception {
        Workout saved = workoutRepository.save(workout("EPIC Day 1", defaultAuthor));

        mvc.perform(delete("/api/workouts/" + saved.getId()))
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteWorkout_unknownId_returns404() throws Exception {
        mvc.perform(delete("/api/workouts/999"))
            .andExpect(status().isNotFound());
    }

    // --- Filters ---

    @Test
    void listWorkouts_filterByMuscleGroup_returnsMatchingOnly() throws Exception {
        workoutRepository.save(workoutWithMuscleGroups("EPIC Day 1", List.of(MuscleGroup.LEGS, MuscleGroup.GLUTES), defaultAuthor));
        workoutRepository.save(workoutWithMuscleGroups("Upper Body", List.of(MuscleGroup.BACK, MuscleGroup.ARMS), defaultAuthor));

        mvc.perform(get("/api/workouts").param("muscleGroup", "LEGS"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name").value("EPIC Day 1"));
    }

    @Test
    void listWorkouts_filterByAuthorId_returnsMatchingOnly() throws Exception {
        Author other = authorRepository.save(author("Jeff Nippard"));
        workoutRepository.save(workout("EPIC Day 1", defaultAuthor));
        workoutRepository.save(workout("Jeff Workout", other));

        mvc.perform(get("/api/workouts").param("authorId", String.valueOf(defaultAuthor.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name").value("EPIC Day 1"));
    }

    @Test
    void listWorkouts_filterByMinDuration_returnsMatchingOnly() throws Exception {
        workoutRepository.save(workoutWithDuration("Short", 20, defaultAuthor));
        workoutRepository.save(workoutWithDuration("Long", 45, defaultAuthor));

        mvc.perform(get("/api/workouts").param("minDuration", "30"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name").value("Long"));
    }

    @Test
    void listWorkouts_filterByMaxDuration_returnsMatchingOnly() throws Exception {
        workoutRepository.save(workoutWithDuration("Short", 20, defaultAuthor));
        workoutRepository.save(workoutWithDuration("Long", 45, defaultAuthor));

        mvc.perform(get("/api/workouts").param("maxDuration", "30"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name").value("Short"));
    }

    @Test
    void listWorkouts_filterByDurationRange_returnsMatchingOnly() throws Exception {
        workoutRepository.save(workoutWithDuration("Too short", 15, defaultAuthor));
        workoutRepository.save(workoutWithDuration("Just right", 30, defaultAuthor));
        workoutRepository.save(workoutWithDuration("Also right", 45, defaultAuthor));
        workoutRepository.save(workoutWithDuration("Too long", 60, defaultAuthor));

        mvc.perform(get("/api/workouts").param("minDuration", "30").param("maxDuration", "45"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void listWorkouts_combinedFilters_returnsMatchingOnly() throws Exception {
        Author other = authorRepository.save(author("Jeff Nippard"));
        workoutRepository.save(workoutWithAll("Match", List.of(MuscleGroup.LEGS), 30, defaultAuthor));
        workoutRepository.save(workoutWithAll("Wrong author", List.of(MuscleGroup.LEGS), 30, other));
        workoutRepository.save(workoutWithAll("Wrong muscle", List.of(MuscleGroup.BACK), 30, defaultAuthor));
        workoutRepository.save(workoutWithAll("Too short", List.of(MuscleGroup.LEGS), 10, defaultAuthor));

        mvc.perform(get("/api/workouts")
                .param("muscleGroup", "LEGS")
                .param("authorId", String.valueOf(defaultAuthor.getId()))
                .param("minDuration", "25"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].name").value("Match"));
    }

    // --- Helpers ---

    private Author author(String name) {
        Author a = new Author();
        a.setName(name);
        return a;
    }

    private Workout workout(String name, Author author) {
        return workoutWithMuscleGroups(name, List.of(MuscleGroup.TOTAL_BODY), author);
    }

    private Workout workoutWithMuscleGroups(String name, List<MuscleGroup> muscleGroups, Author author) {
        Workout w = new Workout();
        w.setName(name);
        w.setMuscleGroups(muscleGroups);
        w.setAuthor(author);
        return w;
    }

    private Workout workoutWithDuration(String name, int durationMinutes, Author author) {
        Workout w = new Workout();
        w.setName(name);
        w.setDurationMinutes(durationMinutes);
        w.setMuscleGroups(List.of(MuscleGroup.TOTAL_BODY));
        w.setAuthor(author);
        return w;
    }

    private Workout workoutWithAll(String name, List<MuscleGroup> muscleGroups, int durationMinutes, Author author) {
        Workout w = new Workout();
        w.setName(name);
        w.setMuscleGroups(muscleGroups);
        w.setDurationMinutes(durationMinutes);
        w.setAuthor(author);
        return w;
    }
}