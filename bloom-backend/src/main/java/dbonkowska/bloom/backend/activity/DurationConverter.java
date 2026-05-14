package dbonkowska.bloom.backend.activity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import java.time.Duration;

@Converter(autoApply = true)
public class DurationConverter implements AttributeConverter<Duration, Integer> {

    @Override
    public Integer convertToDatabaseColumn(Duration duration) {
        return duration == null ? null : Math.toIntExact(duration.getSeconds());
    }

    @Override
    public Duration convertToEntityAttribute(Integer seconds) {
        return seconds == null ? null : Duration.ofSeconds(seconds);
    }
}
