package dbonkowska.bloom.backend.bodymeasurement;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;

public record BodyMeasurementDto(
    @JsonProperty(access = JsonProperty.Access.READ_ONLY) Long id,
    @NotNull @PastOrPresent LocalDate date,
    @Positive Double weight,
    @Positive Double bodyFatPct,
    @Positive Double muscleMass,
    @Positive Double waterPct,
    @Positive Double bonesPct,
    @Positive Double bmi,
    @Positive Double chest,
    @Positive Double waist,
    @Positive Double stomach,
    @Positive Double hips,
    @Positive Double forearmLeft,
    @Positive Double forearmRight,
    @Positive Double armLeft,
    @Positive Double armRight,
    @Positive Double thighLeft,
    @Positive Double thighRight,
    @Positive Double calfLeft,
    @Positive Double calfRight
) {}
