package dbonkowska.bloom.backend.session.garmin;

import dbonkowska.bloom.backend.session.Session;
import dbonkowska.bloom.backend.session.SessionRepository;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;

@Service
public class GarminImportService {

    private final SessionRepository repository;
    private final GarminCsvParser parser;

    public GarminImportService(SessionRepository repository, GarminCsvParser parser) {
        this.repository = repository;
        this.parser = parser;
    }

    public GarminImportResultDto importCsv(InputStream input) throws IOException {
        var parseResult = parser.parse(input);
        int created = 0;
        int skippedDuplicates = 0;

        for (Session session : parseResult.activities()) {
            if (repository.existsByDateAndType(session.getDate(), session.getType())) {
                skippedDuplicates++;
                continue;
            }
            repository.save(session);
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