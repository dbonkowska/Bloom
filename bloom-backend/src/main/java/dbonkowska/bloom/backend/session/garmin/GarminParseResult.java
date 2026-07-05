package dbonkowska.bloom.backend.session.garmin;

import dbonkowska.bloom.backend.session.Session;
import java.util.List;
import java.util.Set;

public record GarminParseResult(
    List<Session> activities,
    Set<String> unknownTypes,
    int unknownTypeCount,
    int malformedCount
) {}