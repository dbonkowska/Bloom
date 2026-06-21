package dbonkowska.bloom.backend.activity.garmin;

import java.util.Set;

public record GarminImportResult(
    int created,
    int skippedDuplicates,
    int skippedUnknownType,
    Set<String> unknownTypes,
    int skippedMalformed
) {}
