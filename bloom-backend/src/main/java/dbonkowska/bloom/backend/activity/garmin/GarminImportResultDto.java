package dbonkowska.bloom.backend.activity.garmin;

import java.util.Set;

public record GarminImportResultDto(
    int created,
    int skippedDuplicates,
    int skippedUnknownType,
    Set<String> unknownTypes,
    int skippedMalformed
) {}
