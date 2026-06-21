package dbonkowska.bloom.backend.activity.garmin;

import dbonkowska.bloom.backend.activity.Activity;
import java.util.List;
import java.util.Set;

public record GarminParseResult(
    List<Activity> activities,
    Set<String> unknownTypes,
    int unknownTypeCount,
    int malformedCount
) {}
