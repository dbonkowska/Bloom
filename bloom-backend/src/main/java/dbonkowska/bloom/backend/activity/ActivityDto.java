package dbonkowska.bloom.backend.activity;

import java.time.LocalDateTime;

public record ActivityDto(
    Long id,
    LocalDateTime date,
    ActivityType type,
    String title,
    Integer durationSeconds,
    Integer distance,
    Integer avgHeartRate,
    Integer maxHeartRate,
    Integer calories
) {}
