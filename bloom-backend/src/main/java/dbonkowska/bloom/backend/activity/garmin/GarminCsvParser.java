package dbonkowska.bloom.backend.activity.garmin;

import dbonkowska.bloom.backend.activity.Activity;
import dbonkowska.bloom.backend.activity.ActivityType;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Component
public class GarminCsvParser {

    private static final Logger log = LoggerFactory.getLogger(GarminCsvParser.class);

    private static final DateTimeFormatter DATE_FORMAT =
        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Map<String, ActivityType> TYPE_MAP = Map.of(
        "Chodzenie", ActivityType.WALKING,
        "Joga", ActivityType.YOGA,
        "Trening siłowy", ActivityType.STRENGTH_TRAINING,
        "Pływanie na basenie", ActivityType.POOL_SWIMMING
    );

    public GarminParseResult parse(InputStream input) throws IOException {
        var activities = new ArrayList<Activity>();
        var unknownTypes = new HashSet<String>();
        int unknownTypeCount = 0;
        int malformedCount = 0;

        var format = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setTrim(true)
            .build();

        try (var reader = new InputStreamReader(input, StandardCharsets.UTF_8)) {
            for (CSVRecord record : format.parse(reader)) {
                try {
                    String rawType = record.get("Typ aktywności");
                    ActivityType type = TYPE_MAP.get(rawType);
                    if (type == null) {
                        unknownTypes.add(rawType);
                        unknownTypeCount++;
                        continue;
                    }
                    Activity a = new Activity();
                    a.setDate(LocalDateTime.parse(record.get("Data"), DATE_FORMAT));
                    a.setType(type);
                    a.setTitle(record.get("Tytuł"));
                    a.setDuration(parseDuration(record.get("Czas")));
                    a.setDistance(parseDistance(record.get("Dystans")));
                    a.setAvgHeartRate(parseNullableInt(record.get("Średnie tętno")));
                    a.setMaxHeartRate(parseNullableInt(record.get("Maksymalne tętno")));
                    a.setCalories(parseNullableInt(record.get("Suma kalorii")));
                    activities.add(a);
                } catch (NumberFormatException | java.time.format.DateTimeParseException | ArrayIndexOutOfBoundsException e) {
                    malformedCount++;
                } catch (Exception e) {
                    log.warn("Unexpected error parsing CSV row: {}", e.getMessage(), e);
                    malformedCount++;
                }
            }
        }

        return new GarminParseResult(activities, Set.copyOf(unknownTypes), unknownTypeCount, malformedCount);
    }

    private Duration parseDuration(String value) {
        String stripped = value.replaceAll("\\.\\d+$", "");
        String[] parts = stripped.split(":");
        long seconds = parts.length == 3
            ? Long.parseLong(parts[0]) * 3600 + Long.parseLong(parts[1]) * 60 + Long.parseLong(parts[2])
            : Long.parseLong(parts[0]) * 60 + Long.parseLong(parts[1]);
        return Duration.ofSeconds(seconds);
    }

    private Integer parseDistance(String value) {
        if ("--".equals(value)) return null;
        if (value.contains(".")) {
            return (int) Math.round(Double.parseDouble(value) * 1000);
        }
        return Integer.parseInt(value);
    }

    private Integer parseNullableInt(String value) {
        return "--".equals(value) ? null : Integer.parseInt(value);
    }
}
