package dbonkowska.bloom.backend.session.garmin;

import dbonkowska.bloom.backend.session.Session;
import dbonkowska.bloom.backend.session.SessionRepository;
import dbonkowska.bloom.backend.session.SessionType;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GarminImportServiceTest {

    @Mock
    private SessionRepository repository;

    @Mock
    private GarminCsvParser parser;

    @InjectMocks
    private GarminImportService service;

    private InputStream emptyStream() { return new ByteArrayInputStream(new byte[0]); }

    @Test
    void importCsv_savesNewSessions() throws Exception {
        when(parser.parse(any())).thenReturn(
            new GarminParseResult(List.of(session()), Set.of(), 0, 0)
        );
        when(repository.existsByDateAndType(any(), any())).thenReturn(false);

        GarminImportResultDto result = service.importCsv(emptyStream());

        assertThat(result.created()).isEqualTo(1);
        assertThat(result.skippedDuplicates()).isZero();
        verify(repository).save(any(Session.class));
    }

    @Test
    void importCsv_skipsDuplicates() throws Exception {
        when(parser.parse(any())).thenReturn(
            new GarminParseResult(List.of(session()), Set.of(), 0, 0)
        );
        when(repository.existsByDateAndType(any(), any())).thenReturn(true);

        GarminImportResultDto result = service.importCsv(emptyStream());

        assertThat(result.skippedDuplicates()).isEqualTo(1);
        assertThat(result.created()).isZero();
        verify(repository, never()).save(any());
    }

    @Test
    void importCsv_reportsUnknownTypesFromParser() throws Exception {
        when(parser.parse(any())).thenReturn(
            new GarminParseResult(List.of(), Set.of("Bieganie"), 1, 0)
        );

        GarminImportResultDto result = service.importCsv(emptyStream());

        assertThat(result.skippedUnknownType()).isEqualTo(1);
        assertThat(result.unknownTypes()).containsExactly("Bieganie");
    }

    @Test
    void importCsv_skippedUnknownType_countsRows_notDistinctTypes() throws Exception {
        when(parser.parse(any())).thenReturn(
            new GarminParseResult(List.of(), Set.of("Bieganie"), 10, 0)
        );

        GarminImportResultDto result = service.importCsv(emptyStream());

        assertThat(result.skippedUnknownType()).isEqualTo(10);
    }

    @Test
    void importCsv_reportsMalformedCountFromParser() throws Exception {
        when(parser.parse(any())).thenReturn(
            new GarminParseResult(List.of(), Set.of(), 0, 3)
        );

        GarminImportResultDto result = service.importCsv(emptyStream());

        assertThat(result.skippedMalformed()).isEqualTo(3);
    }

    @Test
    void importCsv_savesSessionFromParserDirectly() throws Exception {
        Session parsed = session();
        when(parser.parse(any())).thenReturn(
            new GarminParseResult(List.of(parsed), Set.of(), 0, 0)
        );
        when(repository.existsByDateAndType(any(), any())).thenReturn(false);

        service.importCsv(emptyStream());

        verify(repository).existsByDateAndType(parsed.getDate(), parsed.getType());
        verify(repository).save(same(parsed));
    }

    private Session session() {
        Session s = new Session();
        s.setDate(LocalDateTime.of(2026, 1, 10, 10, 0));
        s.setType(SessionType.WALKING);
        s.setTitle("Walk");
        s.setDuration(Duration.ofMinutes(30));
        return s;
    }
}