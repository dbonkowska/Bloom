package dbonkowska.bloom.backend.activity.garmin;

import dbonkowska.bloom.backend.activity.Activity;
import dbonkowska.bloom.backend.activity.ActivityType;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class GarminCsvParserTest {

    private final GarminCsvParser parser = new GarminCsvParser();

    private static final String HEADER =
        "Typ aktywności,Data,Ulubiony,Tytuł,Dystans,Suma kalorii,Czas,Średnie tętno,Maksymalne tętno\n";

    @Test
    void parse_sampleFile_parsesAllKnownRows() throws Exception {
        InputStream csv = getClass().getResourceAsStream("/garmin-sample.csv");
        GarminParseResult result = parser.parse(csv);
        assertThat(result.activities()).hasSize(20);
        assertThat(result.unknownTypes()).isEmpty();
        assertThat(result.malformedCount()).isZero();
    }

    @Test
    void parse_mapsAllKnownTypes() throws Exception {
        InputStream csv = stream(HEADER
            + "Joga,2026-01-01 10:00:00,false,\"Joga\",\"--\",\"50\",\"00:30:00\",\"80\",\"100\"\n"
            + "Trening siłowy,2026-01-02 10:00:00,false,\"Siła\",\"0.00\",\"200\",\"00:30:00\",\"120\",\"150\"\n"
            + "Pływanie na basenie,2026-01-03 10:00:00,false,\"Pływanie\",\"1000\",\"300\",\"00:40:00\",\"140\",\"170\"\n"
            + "Chodzenie,2026-01-04 10:00:00,false,\"Spacer\",\"2.50\",\"100\",\"00:30:00\",\"100\",\"120\"\n"
        );
        GarminParseResult result = parser.parse(csv);
        assertThat(result.activities()).extracting(Activity::getType)
            .containsExactly(
                ActivityType.YOGA,
                ActivityType.STRENGTH_TRAINING,
                ActivityType.POOL_SWIMMING,
                ActivityType.WALKING
            );
    }

    @Test
    void parse_unknownType_isSkippedAndCollected() throws Exception {
        InputStream csv = stream(HEADER
            + "Bieganie,2026-01-01 10:00:00,false,\"Bieg\",\"5.00\",\"400\",\"00:30:00\",\"155\",\"175\"\n"
        );
        GarminParseResult result = parser.parse(csv);
        assertThat(result.activities()).isEmpty();
        assertThat(result.unknownTypes()).containsExactly("Bieganie");
        assertThat(result.unknownTypeCount()).isEqualTo(1);
        assertThat(result.malformedCount()).isZero();
    }

    @Test
    void parse_unknownType_countsByRow_notByDistinctType() throws Exception {
        InputStream csv = stream(HEADER
            + "Bieganie,2026-01-01 10:00:00,false,\"Bieg\",\"5.00\",\"400\",\"00:30:00\",\"155\",\"175\"\n"
            + "Bieganie,2026-01-02 10:00:00,false,\"Bieg\",\"6.00\",\"420\",\"00:31:00\",\"158\",\"178\"\n"
            + "Triathlon,2026-01-03 10:00:00,false,\"Tri\",\"20.00\",\"600\",\"01:00:00\",\"150\",\"180\"\n"
        );
        GarminParseResult result = parser.parse(csv);
        assertThat(result.unknownTypeCount()).isEqualTo(3);
        assertThat(result.unknownTypes()).containsExactlyInAnyOrder("Bieganie", "Triathlon");
    }

    @Test
    void parse_parsesDate() throws Exception {
        InputStream csv = stream(HEADER
            + "Joga,2026-05-03 20:35:27,false,\"Joga\",\"--\",\"23\",\"00:06:25.8\",\"99\",\"118\"\n"
        );
        GarminParseResult result = parser.parse(csv);
        assertThat(result.activities().get(0).getDate())
            .isEqualTo(LocalDateTime.of(2026, 5, 3, 20, 35, 27));
    }

    @Test
    void parse_duration_withFractionalSeconds() throws Exception {
        InputStream csv = stream(HEADER
            + "Joga,2026-01-01 10:00:00,false,\"Joga\",\"--\",\"50\",\"00:06:25.8\",\"--\",\"--\"\n"
        );
        GarminParseResult result = parser.parse(csv);
        assertThat(result.activities().get(0).getDuration()).isEqualTo(Duration.ofSeconds(385));
    }

    @Test
    void parse_duration_withoutFractionalSeconds() throws Exception {
        InputStream csv = stream(HEADER
            + "Trening siłowy,2026-01-01 10:00:00,false,\"Siła\",\"0.00\",\"200\",\"00:34:46\",\"--\",\"--\"\n"
        );
        GarminParseResult result = parser.parse(csv);
        assertThat(result.activities().get(0).getDuration()).isEqualTo(Duration.ofSeconds(2086));
    }

    @Test
    void parse_distance_withDecimal_convertsKmToMeters() throws Exception {
        InputStream csv = stream(HEADER
            + "Chodzenie,2026-01-01 10:00:00,false,\"Spacer\",\"1.11\",\"79\",\"00:14:27\",\"118\",\"125\"\n"
        );
        GarminParseResult result = parser.parse(csv);
        assertThat(result.activities().get(0).getDistance()).isEqualTo(1110);
    }

    @Test
    void parse_distance_withoutDecimal_returnsMeters() throws Exception {
        InputStream csv = stream(HEADER
            + "Pływanie na basenie,2026-01-01 10:00:00,false,\"Pływanie\",\"875\",\"342\",\"00:36:04\",\"153\",\"176\"\n"
        );
        GarminParseResult result = parser.parse(csv);
        assertThat(result.activities().get(0).getDistance()).isEqualTo(875);
    }

    @Test
    void parse_distance_dash_returnsNull() throws Exception {
        InputStream csv = stream(HEADER
            + "Joga,2026-01-01 10:00:00,false,\"Joga\",\"--\",\"23\",\"00:30:00\",\"--\",\"--\"\n"
        );
        GarminParseResult result = parser.parse(csv);
        assertThat(result.activities().get(0).getDistance()).isNull();
    }

    @Test
    void parse_heartRateAndCalories_dash_returnsNull() throws Exception {
        InputStream csv = stream(HEADER
            + "Joga,2026-01-01 10:00:00,false,\"Joga\",\"--\",\"--\",\"00:30:00\",\"--\",\"--\"\n"
        );
        GarminParseResult result = parser.parse(csv);
        Activity a = result.activities().get(0);
        assertThat(a.getCalories()).isNull();
        assertThat(a.getAvgHeartRate()).isNull();
        assertThat(a.getMaxHeartRate()).isNull();
    }

    @Test
    void parse_malformedRow_isSkippedAndCounted() throws Exception {
        InputStream csv = stream(HEADER
            + "Joga,NOT-A-DATE,false,\"Joga\",\"--\",\"50\",\"00:30:00\",\"80\",\"100\"\n"
        );
        GarminParseResult result = parser.parse(csv);
        assertThat(result.activities()).isEmpty();
        assertThat(result.malformedCount()).isEqualTo(1);
    }

    @Test
    void parse_mixedRows_correctCounts() throws Exception {
        InputStream csv = stream(HEADER
            + "Joga,2026-01-01 10:00:00,false,\"Joga\",\"--\",\"50\",\"00:30:00\",\"80\",\"100\"\n"
            + "Bieganie,2026-01-02 10:00:00,false,\"Bieg\",\"5.00\",\"400\",\"00:30:00\",\"155\",\"175\"\n"
            + "Joga,INVALID,false,\"Joga\",\"--\",\"50\",\"00:30:00\",\"80\",\"100\"\n"
        );
        GarminParseResult result = parser.parse(csv);
        assertThat(result.activities()).hasSize(1);
        assertThat(result.unknownTypes()).containsExactly("Bieganie");
        assertThat(result.unknownTypeCount()).isEqualTo(1);
        assertThat(result.malformedCount()).isEqualTo(1);
    }

    private InputStream stream(String csv) {
        return new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8));
    }
}
