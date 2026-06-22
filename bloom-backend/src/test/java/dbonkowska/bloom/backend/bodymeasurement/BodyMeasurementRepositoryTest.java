package dbonkowska.bloom.backend.bodymeasurement;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureDataSourceInitialization;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ImportTestcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@AutoConfigureDataSourceInitialization
class BodyMeasurementRepositoryTest {

    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired
    private TestEntityManager em;

    @Autowired
    private BodyMeasurementRepository repository;

    @Test
    void findAllByOrderByDateDesc_returnsAllSortedNewestFirst() {
        em.persist(measurement(LocalDate.of(2026, 1, 10)));
        em.persist(measurement(LocalDate.of(2026, 1, 20)));
        em.flush();

        List<BodyMeasurement> result = repository.findAllByOrderByDateDesc();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(BodyMeasurement::getDate)
            .containsExactly(LocalDate.of(2026, 1, 20), LocalDate.of(2026, 1, 10));
    }

    @Test
    void findByDateBetweenOrderByDateDesc_returnsOnlyRecordsInRange() {
        em.persist(measurement(LocalDate.of(2026, 1, 5)));
        em.persist(measurement(LocalDate.of(2026, 1, 10)));
        em.persist(measurement(LocalDate.of(2026, 1, 20)));
        em.flush();

        List<BodyMeasurement> result = repository.findByDateBetweenOrderByDateDesc(
            LocalDate.of(2026, 1, 9),
            LocalDate.of(2026, 1, 21)
        );

        assertThat(result).hasSize(2);
        assertThat(result).extracting(BodyMeasurement::getDate)
            .containsExactly(LocalDate.of(2026, 1, 20), LocalDate.of(2026, 1, 10));
    }

    @Test
    void uniqueConstraintOnDate_throwsOnDuplicateInsert() {
        LocalDate date = LocalDate.of(2026, 1, 10);
        em.persist(measurement(date));
        em.flush();

        assertThatThrownBy(() -> {
            em.persist(measurement(date));
            em.flush();
        }).isInstanceOf(Exception.class);
    }

    private BodyMeasurement measurement(LocalDate date) {
        BodyMeasurement m = new BodyMeasurement();
        m.setDate(date);
        m.setWeight(72.5);
        return m;
    }
}
