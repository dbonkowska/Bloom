package dbonkowska.bloom.backend.activity;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findByDateBetween(LocalDateTime from, LocalDateTime to);
}
