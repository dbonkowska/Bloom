package dbonkowska.bloom.backend.calendar;

import dbonkowska.bloom.backend.program.Program;
import dbonkowska.bloom.backend.program.ProgramRepository;
import dbonkowska.bloom.backend.program.day.ProgramDay;
import dbonkowska.bloom.backend.program.workout.ProgramWorkout;
import dbonkowska.bloom.backend.program.session.PlannedSession;
import dbonkowska.bloom.backend.program.session.PlannedSessionRepository;
import dbonkowska.bloom.backend.session.Session;
import dbonkowska.bloom.backend.session.SessionRepository;
import dbonkowska.bloom.backend.session.SessionType;
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
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ImportTestcontainers
class CalendarIntegrationTest {

    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired MockMvc mvc;
    @Autowired SessionRepository sessionRepository;
    @Autowired PlannedSessionRepository plannedSessionRepository;
    @Autowired ProgramRepository programRepository;
    @Autowired WorkoutRepository workoutRepository;
    @Autowired AuthorRepository authorRepository;

    private Workout defaultWorkout;

    @BeforeEach
    void setUp() {
        plannedSessionRepository.deleteAll();
        programRepository.deleteAll();
        sessionRepository.deleteAll();
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

    @Test
    void calendar_sameDate_mergesActualAndPlannedIntoOneBucket() throws Exception {
        sessionRepository.save(actual(LocalDateTime.of(2026, 1, 10, 8, 0)));
        plannedSessionRepository.save(planned(LocalDate.of(2026, 1, 10)));

        mvc.perform(get("/api/calendar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].date").value("2026-01-10"))
            .andExpect(jsonPath("$[0].actual", hasSize(1)))
            .andExpect(jsonPath("$[0].planned", hasSize(1)));
    }

    @Test
    void calendar_onlyPlanned_actualIsEmptyArray() throws Exception {
        plannedSessionRepository.save(planned(LocalDate.of(2026, 1, 10)));

        mvc.perform(get("/api/calendar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].actual", hasSize(0)))
            .andExpect(jsonPath("$[0].planned", hasSize(1)));
    }

    @Test
    void calendar_onlyActual_plannedIsEmptyArray() throws Exception {
        sessionRepository.save(actual(LocalDateTime.of(2026, 1, 10, 8, 0)));

        mvc.perform(get("/api/calendar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].actual", hasSize(1)))
            .andExpect(jsonPath("$[0].planned", hasSize(0)));
    }

    @Test
    void calendar_noParams_returnsAllBucketsAscending_emptyDatesOmitted() throws Exception {
        plannedSessionRepository.save(planned(LocalDate.of(2026, 1, 20)));
        sessionRepository.save(actual(LocalDateTime.of(2026, 1, 5, 8, 0)));
        plannedSessionRepository.save(planned(LocalDate.of(2026, 1, 10)));

        mvc.perform(get("/api/calendar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(3)))
            .andExpect(jsonPath("$[0].date").value("2026-01-05"))
            .andExpect(jsonPath("$[1].date").value("2026-01-10"))
            .andExpect(jsonPath("$[2].date").value("2026-01-20"));
    }

    @Test
    void calendar_rangeInclusiveOfBothEndpoints_includesLateSessionOnToDay() throws Exception {
        sessionRepository.save(actual(LocalDateTime.of(2026, 1, 1, 0, 0)));    // on `from`
        sessionRepository.save(actual(LocalDateTime.of(2026, 1, 31, 23, 0)));  // 23:00 on `to`
        sessionRepository.save(actual(LocalDateTime.of(2026, 2, 1, 0, 0)));    // just past `to` — excluded

        mvc.perform(get("/api/calendar")
                .param("from", "2026-01-01")
                .param("to", "2026-01-31"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].date").value("2026-01-01"))
            .andExpect(jsonPath("$[1].date").value("2026-01-31"));
    }

    @Test
    void calendar_fromOnly_returnsSubsetFromDateOnward() throws Exception {
        plannedSessionRepository.save(planned(LocalDate.of(2026, 1, 5)));
        plannedSessionRepository.save(planned(LocalDate.of(2026, 1, 15)));
        plannedSessionRepository.save(planned(LocalDate.of(2026, 1, 25)));

        mvc.perform(get("/api/calendar").param("from", "2026-01-15"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].date").value("2026-01-15"))
            .andExpect(jsonPath("$[1].date").value("2026-01-25"));
    }

    @Test
    void calendar_toOnly_returnsSubsetUpToDateInclusive() throws Exception {
        plannedSessionRepository.save(planned(LocalDate.of(2026, 1, 5)));
        plannedSessionRepository.save(planned(LocalDate.of(2026, 1, 15)));
        plannedSessionRepository.save(planned(LocalDate.of(2026, 1, 25)));

        mvc.perform(get("/api/calendar").param("to", "2026-01-15"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("$[0].date").value("2026-01-05"))
            .andExpect(jsonPath("$[1].date").value("2026-01-15"));
    }

    @Test
    void calendar_fromAfterTo_returns400() throws Exception {
        mvc.perform(get("/api/calendar")
                .param("from", "2026-02-01")
                .param("to", "2026-01-01"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void calendar_withinBucket_actualSortedByTimeAsc_plannedSortedByIdAsc() throws Exception {
        // Insertion order deliberately differs from datetime order so the sort must run
        sessionRepository.save(actual(LocalDateTime.of(2026, 1, 10, 14, 0)));
        sessionRepository.save(actual(LocalDateTime.of(2026, 1, 10, 8, 0)));

        PlannedSession firstPlanned = plannedSessionRepository.save(planned(LocalDate.of(2026, 1, 10)));
        PlannedSession secondPlanned = plannedSessionRepository.save(planned(LocalDate.of(2026, 1, 10)));

        mvc.perform(get("/api/calendar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].actual", hasSize(2)))
            .andExpect(jsonPath("$[0].actual[0].date").value("2026-01-10T08:00:00"))
            .andExpect(jsonPath("$[0].actual[1].date").value("2026-01-10T14:00:00"))
            .andExpect(jsonPath("$[0].planned", hasSize(2)))
            .andExpect(jsonPath("$[0].planned[0].id").value(firstPlanned.getId().intValue()))
            .andExpect(jsonPath("$[0].planned[1].id").value(secondPlanned.getId().intValue()));
    }

    @Test
    void calendar_plannedSortedByProgramWorkoutOrder_warmupBeforeMain() throws Exception {
        Workout warmup = workout("Warmup");
        Workout main = workout("Main");

        // Program day where the warmup is order 1 and the main workout is order 2
        Program program = new Program();
        program.setName("Day Plan");
        ProgramDay day = new ProgramDay();
        day.setDayNumber(1);
        day.setProgram(program);
        ProgramWorkout pwWarmup = programWorkout(day, warmup, 1);
        ProgramWorkout pwMain = programWorkout(day, main, 2);
        day.setWorkouts(List.of(pwWarmup, pwMain));
        program.setDays(List.of(day));
        program = programRepository.save(program);

        ProgramWorkout savedWarmup = orderedWorkout(program, 1);
        ProgramWorkout savedMain = orderedWorkout(program, 2);

        // Insert main first so id order is the reverse of the program order
        LocalDate date = LocalDate.of(2026, 1, 10);
        plannedSessionRepository.save(plannedFor(date, savedMain));
        plannedSessionRepository.save(plannedFor(date, savedWarmup));

        mvc.perform(get("/api/calendar"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].planned", hasSize(2)))
            .andExpect(jsonPath("$[0].planned[0].workout.name").value("Warmup"))
            .andExpect(jsonPath("$[0].planned[1].workout.name").value("Main"));
    }

    private Workout workout(String name) {
        Workout w = new Workout();
        w.setName(name);
        w.setAuthor(authorRepository.findAll().getFirst());
        w.setMuscleGroups(List.of(MuscleGroup.TOTAL_BODY));
        return workoutRepository.save(w);
    }

    private ProgramWorkout programWorkout(ProgramDay day, Workout workout, int order) {
        ProgramWorkout pw = new ProgramWorkout();
        pw.setProgramDay(day);
        pw.setWorkout(workout);
        pw.setOrder(order);
        return pw;
    }

    private ProgramWorkout orderedWorkout(Program program, int order) {
        return program.getDays().getFirst().getWorkouts().stream()
            .filter(pw -> pw.getOrder() == order)
            .findFirst().orElseThrow();
    }

    private PlannedSession plannedFor(LocalDate date, ProgramWorkout pw) {
        PlannedSession p = new PlannedSession();
        p.setDate(date);
        p.setWorkout(pw.getWorkout());
        p.setProgramWorkout(pw);
        p.setCompleted(false);
        return p;
    }

    private Session actual(LocalDateTime dateTime) {
        Session s = new Session();
        s.setDate(dateTime);
        s.setType(SessionType.STRENGTH_TRAINING);
        s.setTitle("Session");
        s.setDuration(Duration.ofMinutes(30));
        return s;
    }

    private PlannedSession planned(LocalDate date) {
        PlannedSession p = new PlannedSession();
        p.setDate(date);
        p.setWorkout(defaultWorkout);
        p.setCompleted(false);
        return p;
    }
}