package dbonkowska.bloom.backend.activity.garmin;

import dbonkowska.bloom.backend.activity.Activity;
import dbonkowska.bloom.backend.activity.ActivityRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class GarminImportService {

    private final ActivityRepository repository;
    private final GarminCsvParser parser;

    public GarminImportService(ActivityRepository repository, GarminCsvParser parser) {
        this.repository = repository;
        this.parser = parser;
    }

    public GarminImportResultDto importCsv(InputStream input) throws IOException {
        var parseResult = parser.parse(input);
        int created = 0;
        int skippedDuplicates = 0;

        for (Activity activity : parseResult.activities()) {
            if (repository.existsByDateAndType(activity.getDate(), activity.getType())) {
                skippedDuplicates++;
                continue;
            }
            repository.save(activity);
            created++;
        }

        return new GarminImportResultDto(
            created,
            skippedDuplicates,
            parseResult.unknownTypeCount(),
            parseResult.unknownTypes(),
            parseResult.malformedCount()
        );
    }
}
