package dbonkowska.bloom.backend.program;

import dbonkowska.bloom.backend.workout.MuscleGroup;
import dbonkowska.bloom.backend.workout.Workout;
import dbonkowska.bloom.backend.workout.WorkoutRepository;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ImportTestcontainers
class ProgramIntegrationTest {

    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired MockMvc mvc;
    @Autowired ProgramRepository programRepository;
    @Autowired ProgramCycleRepository programCycleRepository;
    @Autowired PlannedSessionRepository plannedSessionRepository;
    @Autowired WorkoutRepository workoutRepository;
    @Autowired AuthorRepository authorRepository;

    private Workout defaultWorkout;

    @BeforeEach
    void setUp() {
        plannedSessionRepository.deleteAll();
        programCycleRepository.deleteAll();
        programRepository.deleteAll();
        workoutRepository.deleteAll();
        authorRepository.deleteAll();

        Author author = new Author();
        author.setName("Default Author");
        author = authorRepository.save(author);

        defaultWorkout = new Workout();
        defaultWorkout.setName("Default Workout");
        defaultWorkout.setAuthor(author);
        defaultWorkout.setMuscleGroups(List.of(MuscleGroup.TOTAL_BODY));
        defaultWorkout = workoutRepository.save(defaultWorkout);
    }

    // --- POST /api/programs ---

    @Test
    void createProgram_withDaysAndWorkouts_returns201() throws Exception {
        mvc.perform(post("/api/programs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Marathon Prep",
                        "description": "12 weeks",
                        "days": [
                            {"workouts": [{"workoutId": %d}]},
                            {"workouts": []}
                        ]
                    }
                    """.formatted(defaultWorkout.getId())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.days", hasSize(2)))
            .andExpect(jsonPath("$.days[0].workouts[0].workout.name").value(defaultWorkout.getName()))
            .andExpect(jsonPath("$.days[1].workouts", hasSize(0)));
    }

    @Test
    void createProgram_requiredFieldsOnly_returns201() throws Exception {
        mvc.perform(post("/api/programs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "Empty Plan"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.days", hasSize(0)));
    }

    @Test
    void createProgram_missingName_returns400() throws Exception {
        mvc.perform(post("/api/programs")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    // --- GET /api/programs ---

    @Test
    void listPrograms_returnsSummariesWithTotalDays() throws Exception {
        programRepository.save(programWithDays("Plan A", 2));
        programRepository.save(programWithDays("Plan B", 1));

        mvc.perform(get("/api/programs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].totalDays").isNumber())
            .andExpect(jsonPath("$[1].totalDays").isNumber())
            .andExpect(jsonPath("$[0].days").doesNotExist());
    }

    @Test
    void listPrograms_emptyDb_returnsEmptyArray() throws Exception {
        mvc.perform(get("/api/programs"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));
    }

    // --- GET /api/programs/{id} ---

    @Test
    void getProgram_existingId_returnsFullDtoWithDays() throws Exception {
        Program saved = programRepository.save(programWithWorkout("My Program", defaultWorkout));

        mvc.perform(get("/api/programs/" + saved.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(saved.getId()))
            .andExpect(jsonPath("$.days", hasSize(1)))
            .andExpect(jsonPath("$.days[0].workouts[0].workout.name").value(defaultWorkout.getName()));
    }

    @Test
    void getProgram_unknownId_returns404() throws Exception {
        mvc.perform(get("/api/programs/999"))
            .andExpect(status().isNotFound());
    }

    // --- PUT /api/programs/{id} ---

    @Test
    void updateProgram_replacesAllDays_returns200() throws Exception {
        Workout other = workoutRepository.save(workout("Other Workout"));
        Program saved = programRepository.save(programWithDays("Plan A", 2));

        mvc.perform(put("/api/programs/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "name": "Plan A Updated",
                        "days": [{"workouts": [{"workoutId": %d}]}]
                    }
                    """.formatted(other.getId())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Plan A Updated"))
            .andExpect(jsonPath("$.days", hasSize(1)))
            .andExpect(jsonPath("$.days[0].workouts[0].workout.name").value("Other Workout"));
    }

    @Test
    void updateProgram_unknownId_returns404() throws Exception {
        mvc.perform(put("/api/programs/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name": "Updated"}
                    """))
            .andExpect(status().isNotFound());
    }

    // --- DELETE /api/programs/{id} ---

    @Test
    void deleteProgram_noCycles_returns204() throws Exception {
        Program saved = programRepository.save(programWithDays("Plan A", 1));

        mvc.perform(delete("/api/programs/" + saved.getId()))
            .andExpect(status().isNoContent());

        mvc.perform(get("/api/programs/" + saved.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteProgram_unknownId_returns404() throws Exception {
        mvc.perform(delete("/api/programs/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteProgram_withCycles_returns409() throws Exception {
        Program saved = programRepository.save(programWithDays("Plan A", 1));
        ProgramCycle cycle = new ProgramCycle();
        cycle.setProgram(saved);
        cycle.setStartDate(java.time.LocalDate.of(2026, 1, 1));
        programCycleRepository.save(cycle);

        mvc.perform(delete("/api/programs/" + saved.getId()))
            .andExpect(status().isConflict());
    }

    // --- Helpers ---

    private Program programWithDays(String name, int numDays) {
        Program p = new Program();
        p.setName(name);
        List<ProgramDay> days = new ArrayList<>();
        for (int i = 1; i <= numDays; i++) {
            ProgramDay day = new ProgramDay();
            day.setDayNumber(i);
            day.setProgram(p);
            day.setWorkouts(new ArrayList<>());
            days.add(day);
        }
        p.setDays(days);
        return p;
    }

    private Workout workout(String name) {
        Workout w = new Workout();
        w.setName(name);
        w.setAuthor(authorRepository.findAll().getFirst());
        w.setMuscleGroups(List.of(MuscleGroup.TOTAL_BODY));
        return workoutRepository.save(w);
    }

    private Program programWithWorkout(String name, Workout workout) {
        Program p = new Program();
        p.setName(name);
        ProgramDay day = new ProgramDay();
        day.setDayNumber(1);
        day.setProgram(p);
        ProgramWorkout pw = new ProgramWorkout();
        pw.setProgramDay(day);
        pw.setWorkout(workout);
        pw.setOrder(1);
        day.setWorkouts(List.of(pw));
        p.setDays(List.of(day));
        return p;
    }
}