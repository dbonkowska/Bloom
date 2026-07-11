package dbonkowska.bloom.backend.calendar;

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
    @Autowired WorkoutRepository workoutRepository;
    @Autowired AuthorRepository authorRepository;

    private Workout defaultWorkout;

    @BeforeEach
    void setUp() {
        plannedSessionRepository.deleteAll();
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