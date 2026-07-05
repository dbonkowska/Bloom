package dbonkowska.bloom.backend.session.garmin;

import java.util.Set;

public record GarminImportResultDto(
    int created,
    int skippedDuplicates,
    int skippedUnknownType,
    Set<String> unknownTypes,
    int skippedMalformed
) {}