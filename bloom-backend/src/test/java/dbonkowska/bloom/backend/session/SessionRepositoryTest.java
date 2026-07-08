package dbonkowska.bloom.backend.session;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureDataSourceInitialization;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ImportTestcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureDataSourceInitialization
class SessionRepositoryTest {

    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private TestEntityManager em;

    @Autowired
    private SessionRepository repository;

    @Test
    void findAll_returnsAllSessions() {
        em.persist(session(LocalDateTime.of(2026, 1, 10, 10, 0)));
        em.persist(session(LocalDateTime.of(2026, 1, 20, 10, 0)));
        em.flush();

        assertThat(repository.findAll()).hasSize(2);
    }

    @Test
    void findByDateBetween_returnsOnlySessionsInRange() {
        em.persist(session(LocalDateTime.of(2026, 1, 5, 10, 0)));
        em.persist(session(LocalDateTime.of(2026, 1, 10, 10, 0)));
        em.persist(session(LocalDateTime.of(2026, 1, 20, 10, 0)));
        em.flush();

        List<Session> result = repository.findByDateBetween(
            LocalDateTime.of(2026, 1, 9, 0, 0),
            LocalDateTime.of(2026, 1, 21, 0, 0)
        );

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Session::getDate)
            .containsExactlyInAnyOrder(
                LocalDateTime.of(2026, 1, 10, 10, 0),
                LocalDateTime.of(2026, 1, 20, 10, 0)
            );
    }

    @Test
    void findByDateBetween_boundaryDatesAreInclusive() {
        LocalDateTime boundary = LocalDateTime.of(2026, 1, 10, 10, 0);
        em.persist(session(boundary));
        em.flush();

        assertThat(repository.findByDateBetween(boundary, boundary)).hasSize(1);
    }

    @Test
    void existsByDateAndType_returnsTrueWhenMatch() {
        LocalDateTime date = LocalDateTime.of(2026, 1, 10, 10, 0);
        em.persist(session(date));
        em.flush();

        assertThat(repository.existsByDateAndType(date, SessionType.WALKING)).isTrue();
    }

    @Test
    void existsByDateAndType_returnsFalseWhenNoMatch() {
        LocalDateTime date = LocalDateTime.of(2026, 1, 10, 10, 0);
        em.persist(session(date));
        em.flush();

        assertThat(repository.existsByDateAndType(date, SessionType.YOGA)).isFalse();
        assertThat(repository.existsByDateAndType(LocalDateTime.of(2026, 2, 1, 10, 0), SessionType.WALKING)).isFalse();
    }

    private Session session(LocalDateTime date) {
        Session s = new Session();
        s.setDate(date);
        s.setType(SessionType.WALKING);
        s.setTitle("Test walk");
        s.setDuration(Duration.ofMinutes(30));
        return s;
    }
}