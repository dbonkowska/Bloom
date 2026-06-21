package dbonkowska.bloom.backend.activity;

import org.junit.jupiter.api.Test;
import java.time.Duration;
import static org.assertj.core.api.Assertions.assertThat;

class ActivityDurationConverterTest {

    private final ActivityDurationConverter converter = new ActivityDurationConverter();

    @Test
    void convertToDatabaseColumn_convertsToSeconds() {
        assertThat(converter.convertToDatabaseColumn(Duration.ofMinutes(30))).isEqualTo(1800);
    }

    @Test
    void convertToEntityAttribute_convertsToDuration() {
        assertThat(converter.convertToEntityAttribute(1800)).isEqualTo(Duration.ofMinutes(30));
    }

    @Test
    void convertToDatabaseColumn_nullReturnsNull() {
        assertThat(converter.convertToDatabaseColumn(null)).isNull();
    }

    @Test
    void convertToEntityAttribute_nullReturnsNull() {
        assertThat(converter.convertToEntityAttribute(null)).isNull();
    }
}
