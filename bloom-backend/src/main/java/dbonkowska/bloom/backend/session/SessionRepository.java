package dbonkowska.bloom.backend.session;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDateTime;
import java.util.List;

public interface SessionRepository extends JpaRepository<Session, Long> {
    List<Session> findByDateBetween(LocalDateTime from, LocalDateTime to);
    boolean existsByDateAndType(LocalDateTime date, SessionType type);
}