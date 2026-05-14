package dbonkowska.bloom.backend.activity;

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
class ActivityRepositoryTest {

    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ActivityRepository repository;

    @Test
    void findAll_returnsAllActivities() {
        em.persist(activity(LocalDateTime.of(2026, 1, 10, 10, 0)));
        em.persist(activity(LocalDateTime.of(2026, 1, 20, 10, 0)));
        em.flush();

        assertThat(repository.findAll()).hasSize(2);
    }

    @Test
    void findByDateBetween_returnsOnlyActivitiesInRange() {
        em.persist(activity(LocalDateTime.of(2026, 1, 5, 10, 0)));
        em.persist(activity(LocalDateTime.of(2026, 1, 10, 10, 0)));
        em.persist(activity(LocalDateTime.of(2026, 1, 20, 10, 0)));
        em.flush();

        List<Activity> result = repository.findByDateBetween(
            LocalDateTime.of(2026, 1, 9, 0, 0),
            LocalDateTime.of(2026, 1, 21, 0, 0)
        );

        assertThat(result).hasSize(2);
        assertThat(result).extracting(Activity::getDate)
            .containsExactlyInAnyOrder(
                LocalDateTime.of(2026, 1, 10, 10, 0),
                LocalDateTime.of(2026, 1, 20, 10, 0)
            );
    }

    @Test
    void findByDateBetween_boundaryDatesAreInclusive() {
        LocalDateTime boundary = LocalDateTime.of(2026, 1, 10, 10, 0);
        em.persist(activity(boundary));
        em.flush();

        assertThat(repository.findByDateBetween(boundary, boundary)).hasSize(1);
    }

    private Activity activity(LocalDateTime date) {
        Activity a = new Activity();
        a.setDate(date);
        a.setType(ActivityType.WALKING);
        a.setTitle("Test walk");
        a.setDuration(Duration.ofMinutes(30));
        return a;
    }
}