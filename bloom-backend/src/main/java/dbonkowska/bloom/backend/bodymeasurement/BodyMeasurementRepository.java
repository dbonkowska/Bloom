package dbonkowska.bloom.backend.bodymeasurement;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface BodyMeasurementRepository extends JpaRepository<BodyMeasurement, Long> {
    List<BodyMeasurement> findAllByOrderByDateDesc();
    List<BodyMeasurement> findByDateBetweenOrderByDateDesc(LocalDate from, LocalDate to);
}
