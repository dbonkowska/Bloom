package dbonkowska.bloom.backend.program;

import dbonkowska.bloom.backend.program.cycle.ProgramCycle;
import dbonkowska.bloom.backend.program.cycle.ProgramCycleRepository;
import dbonkowska.bloom.backend.program.day.ProgramDay;
import dbonkowska.bloom.backend.program.workout.ProgramWorkout;
import dbonkowska.bloom.backend.program.session.PlannedSession;
import dbonkowska.bloom.backend.program.session.PlannedSessionRepository;
import dbonkowska.bloom.backend.workout.MuscleGroup;
import dbonkowska.bloom.backend.workout.Workout;
import dbonkowska.bloom.backend.workout.WorkoutRepository;
import dbonkowska.bloom.backend.workout.author.Author;
import dbonkowska.bloom.backend.workout.author.AuthorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import static org.assertj.core.api.Assertions.assertThat;
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
            .andExpect(jsonPath("$.days[0].workouts[0].order").value(1))
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
            .andExpect(jsonPath("$.days[0].workouts[0].workout.name").value(defaultWorkout.getName()))
            .andExpect(jsonPath("$.days[0].workouts[0].order").value(1));
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

    // --- POST /api/programs/{id}/cycles ---

    @Test
    void createCycle_generatesSessionsForWorkoutDays() throws Exception {
        Program program = programRepository.save(programWithStructure(1, 0, 2));

        mvc.perform(post("/api/programs/" + program.getId() + "/cycles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"startDate":"2026-01-01"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.startDate").value("2026-01-01"))
            .andExpect(jsonPath("$.endDate").value("2026-01-03"))
            .andExpect(jsonPath("$.programName").value("Test Program"));

        var sessions = plannedSessionRepository.findAll();
        assertThat(sessions).hasSize(3);
        assertThat(sessions.stream().filter(s -> s.getDate().equals(LocalDate.of(2026, 1, 1))).count()).isEqualTo(1);
        assertThat(sessions.stream().filter(s -> s.getDate().equals(LocalDate.of(2026, 1, 2))).count()).isZero();
        assertThat(sessions.stream().filter(s -> s.getDate().equals(LocalDate.of(2026, 1, 3))).count()).isEqualTo(2);
        assertThat(sessions).allMatch(s -> s.getProgramCycle() != null && s.getProgramWorkout() != null);
    }

    @Test
    void createCycle_restDaysGenerateNoSessions() throws Exception {
        Program program = programRepository.save(programWithStructure(0));

        mvc.perform(post("/api/programs/" + program.getId() + "/cycles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"startDate":"2026-06-01"}
                    """))
            .andExpect(status().isCreated());

        assertThat(plannedSessionRepository.count()).isZero();
    }

    @Test
    void createCycle_missingStartDate_returns400() throws Exception {
        Program program = programRepository.save(programWithDays("Plan A", 1));

        mvc.perform(post("/api/programs/" + program.getId() + "/cycles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createCycle_duplicateStartDate_returns409() throws Exception {
        Program program = programRepository.save(programWithDays("Plan A", 1));
        programCycleRepository.save(cycle(program, LocalDate.of(2026, 1, 1)));

        mvc.perform(post("/api/programs/" + program.getId() + "/cycles")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"startDate":"2026-01-01"}
                    """))
            .andExpect(status().isConflict());
    }

    // --- GET /api/programs/{id}/cycles ---

    @Test
    void listCycles_returnsAllForProgram() throws Exception {
        Program program = programRepository.save(programWithDays("Plan A", 1));
        programCycleRepository.save(cycle(program, LocalDate.of(2026, 1, 1)));
        programCycleRepository.save(cycle(program, LocalDate.of(2026, 2, 1)));

        mvc.perform(get("/api/programs/" + program.getId() + "/cycles"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    // --- GET /api/program-cycles/{id} ---

    @Test
    void getCycle_unknownId_returns404() throws Exception {
        mvc.perform(get("/api/program-cycles/999"))
            .andExpect(status().isNotFound());
    }

    // --- DELETE /api/program-cycles/{id} ---

    @Test
    void deleteCycle_cascadesToPlannedSessions() throws Exception {
        Program program = programRepository.save(programWithDays("Plan A", 1));
        ProgramCycle savedCycle = programCycleRepository.save(cycle(program, LocalDate.of(2026, 1, 1)));

        PlannedSession s1 = new PlannedSession();
        s1.setDate(LocalDate.of(2026, 1, 1));
        s1.setWorkout(defaultWorkout);
        s1.setProgramCycle(savedCycle);
        s1.setCompleted(false);
        PlannedSession s2 = new PlannedSession();
        s2.setDate(LocalDate.of(2026, 1, 2));
        s2.setWorkout(defaultWorkout);
        s2.setProgramCycle(savedCycle);
        s2.setCompleted(false);
        plannedSessionRepository.saveAll(List.of(s1, s2));

        mvc.perform(delete("/api/program-cycles/" + savedCycle.getId()))
            .andExpect(status().isNoContent());

        assertThat(plannedSessionRepository.count()).isZero();
    }

    // --- POST /api/planned-sessions ---

    @Test
    void createPlannedSession_adHoc_returns201() throws Exception {
        mvc.perform(post("/api/planned-sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"date":"2026-03-10","workoutId":%d}
                    """.formatted(defaultWorkout.getId())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.date").value("2026-03-10"))
            .andExpect(jsonPath("$.workout.name").value(defaultWorkout.getName()))
            .andExpect(jsonPath("$.programCycleId").value(org.hamcrest.Matchers.nullValue()))
            .andExpect(jsonPath("$.programWorkoutId").value(org.hamcrest.Matchers.nullValue()))
            .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void createPlannedSession_withProgramCycleId_returns201() throws Exception {
        Program program = programRepository.save(programWithDays("Plan A", 1));
        ProgramCycle savedCycle = programCycleRepository.save(cycle(program, LocalDate.of(2026, 1, 1)));

        mvc.perform(post("/api/planned-sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"date":"2026-03-10","workoutId":%d,"programCycleId":%d}
                    """.formatted(defaultWorkout.getId(), savedCycle.getId())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.programCycleId").value(savedCycle.getId().intValue()));
    }

    @Test
    void createPlannedSession_unknownWorkoutId_returns400() throws Exception {
        mvc.perform(post("/api/planned-sessions")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"date":"2026-03-10","workoutId":999}
                    """))
            .andExpect(status().isBadRequest());
    }

    // --- GET /api/planned-sessions ---

    @Test
    void listPlannedSessions_withDateRange_returnsFiltered() throws Exception {
        plannedSessionRepository.saveAll(List.of(
            session(LocalDate.of(2026, 1, 1)),
            session(LocalDate.of(2026, 1, 15)),
            session(LocalDate.of(2026, 2, 1))
        ));

        mvc.perform(get("/api/planned-sessions")
                .param("from", "2026-01-01")
                .param("to", "2026-01-31"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void listPlannedSessions_noParams_returnsAll() throws Exception {
        plannedSessionRepository.saveAll(List.of(
            session(LocalDate.of(2026, 1, 1)),
            session(LocalDate.of(2026, 2, 1))
        ));

        mvc.perform(get("/api/planned-sessions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)));
    }

    // --- GET /api/planned-sessions/{id} ---

    @Test
    void getPlannedSession_unknownId_returns404() throws Exception {
        mvc.perform(get("/api/planned-sessions/999"))
            .andExpect(status().isNotFound());
    }

    // --- DELETE /api/planned-sessions/{id} ---

    @Test
    void deletePlannedSession_returns204() throws Exception {
        PlannedSession saved = plannedSessionRepository.save(session(LocalDate.of(2026, 1, 1)));

        mvc.perform(delete("/api/planned-sessions/" + saved.getId()))
            .andExpect(status().isNoContent());

        mvc.perform(get("/api/planned-sessions/" + saved.getId()))
            .andExpect(status().isNotFound());
    }

    @Test
    void deletePlannedSession_unknownId_returns404() throws Exception {
        mvc.perform(delete("/api/planned-sessions/999"))
            .andExpect(status().isNotFound());
    }

    // --- PATCH /api/planned-sessions/{id}/reschedule ---

    @Test
    void reschedulePlannedSession_updatesDate() throws Exception {
        PlannedSession saved = plannedSessionRepository.save(session(LocalDate.of(2026, 3, 10)));

        mvc.perform(patch("/api/planned-sessions/" + saved.getId() + "/reschedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"date":"2026-03-15"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.date").value("2026-03-15"))
            .andExpect(jsonPath("$.workout.name").value(defaultWorkout.getName()))
            .andExpect(jsonPath("$.programCycleId").value(org.hamcrest.Matchers.nullValue()))
            .andExpect(jsonPath("$.programWorkoutId").value(org.hamcrest.Matchers.nullValue()));
    }

    @Test
    void reschedulePlannedSession_unknownId_returns404() throws Exception {
        mvc.perform(patch("/api/planned-sessions/999/reschedule")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"date":"2026-03-15"}
                    """))
            .andExpect(status().isNotFound());
    }

    // --- PATCH /api/planned-sessions/{id}/complete ---

    @Test
    void completePlannedSession_setsCompletedTrue() throws Exception {
        PlannedSession saved = plannedSessionRepository.save(session(LocalDate.of(2026, 3, 10)));

        mvc.perform(patch("/api/planned-sessions/" + saved.getId() + "/complete"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    void completePlannedSession_alreadyComplete_isIdempotent() throws Exception {
        PlannedSession s = session(LocalDate.of(2026, 3, 10));
        s.setCompleted(true);
        PlannedSession saved = plannedSessionRepository.save(s);

        mvc.perform(patch("/api/planned-sessions/" + saved.getId() + "/complete"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.completed").value(true));
    }

    // --- PATCH /api/planned-sessions/{id}/incomplete ---

    @Test
    void incompletePlannedSession_setsCompletedFalse() throws Exception {
        PlannedSession s = session(LocalDate.of(2026, 3, 10));
        s.setCompleted(true);
        PlannedSession saved = plannedSessionRepository.save(s);

        mvc.perform(patch("/api/planned-sessions/" + saved.getId() + "/incomplete"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.completed").value(false));
    }

    @Test
    void incompletePlannedSession_alreadyIncomplete_isIdempotent() throws Exception {
        PlannedSession saved = plannedSessionRepository.save(session(LocalDate.of(2026, 3, 10)));

        mvc.perform(patch("/api/planned-sessions/" + saved.getId() + "/incomplete"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.completed").value(false));
    }

    private PlannedSession session(LocalDate date) {
        PlannedSession s = new PlannedSession();
        s.setDate(date);
        s.setWorkout(defaultWorkout);
        s.setCompleted(false);
        return s;
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

    private Program programWithStructure(int... workoutsPerDay) {
        Program p = new Program();
        p.setName("Test Program");
        List<ProgramDay> days = new ArrayList<>();
        for (int i = 0; i < workoutsPerDay.length; i++) {
            ProgramDay day = new ProgramDay();
            day.setDayNumber(i + 1);
            day.setProgram(p);
            List<ProgramWorkout> workouts = new ArrayList<>();
            for (int j = 0; j < workoutsPerDay[i]; j++) {
                ProgramWorkout pw = new ProgramWorkout();
                pw.setProgramDay(day);
                pw.setWorkout(defaultWorkout);
                pw.setOrder(j + 1);
                workouts.add(pw);
            }
            day.setWorkouts(workouts);
            days.add(day);
        }
        p.setDays(days);
        return p;
    }

    private ProgramCycle cycle(Program program, LocalDate startDate) {
        ProgramCycle c = new ProgramCycle();
        c.setProgram(program);
        c.setStartDate(startDate);
        return c;
    }
}