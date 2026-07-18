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
) {

    public static SessionDto from(Session s) {
        return new SessionDto(
            s.getId(),
            s.getDate(),
            s.getType(),
            s.getTitle(),
            Math.toIntExact(s.getDuration().getSeconds()),
            s.getDistance(),
            s.getAvgHeartRate(),
            s.getMaxHeartRate(),
            s.getCalories()
        );
    }
}