package dbonkowska.bloom.backend.session;

import java.time.LocalDateTime;

public record SessionDto(
    Long id,
    LocalDateTime date,
    SessionType type,
    String title,
    Integer durationSeconds,
    Integer distance,
    Integer avgHeartRate,
    Integer maxHeartRate,
    Integer calories
) {}