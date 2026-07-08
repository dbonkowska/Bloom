package dbonkowska.bloom.backend.session;

import dbonkowska.bloom.backend.session.garmin.GarminImportResultDto;
import dbonkowska.bloom.backend.session.garmin.GarminImportService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SessionController.class)
class SessionControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private SessionRepository repository;

    @MockitoBean
    private GarminImportService importService;

    @Test
    void getSessions_noParams_returnsAll() throws Exception {
        when(repository.findAll()).thenReturn(List.of(
            session(1L, LocalDateTime.of(2026, 1, 10, 10, 0))
        ));

        mvc.perform(get("/api/sessions"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].type").value("WALKING"))
            .andExpect(jsonPath("$[0].durationSeconds").value(1800));
    }

    @Test
    void getSessions_withDateRange_returnsFiltered() throws Exception {
        when(repository.findByDateBetween(
            eq(LocalDateTime.of(2026, 1, 10, 0, 0)),
            eq(LocalDateTime.of(2026, 1, 20, 0, 0))
        )).thenReturn(List.of(
            session(2L, LocalDateTime.of(2026, 1, 15, 10, 0))
        ));

        mvc.perform(get("/api/sessions")
                .param("from", "2026-01-10T00:00:00")
                .param("to", "2026-01-20T00:00:00"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    void getSessions_fromAfterTo_returns400() throws Exception {
        mvc.perform(get("/api/sessions")
                .param("from", "2026-01-20T00:00:00")
                .param("to", "2026-01-10T00:00:00"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getSessions_withOnlyOneParam_returnsAll() throws Exception {
        when(repository.findAll()).thenReturn(List.of(
            session(1L, LocalDateTime.of(2026, 1, 10, 10, 0))
        ));

        mvc.perform(get("/api/sessions").param("from", "2026-01-10T00:00:00"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void importCsv_returnsImportSummary() throws Exception {
        when(importService.importCsv(any())).thenReturn(
            new GarminImportResultDto(18, 2, 1, Set.of("Bieganie"), 0)
        );

        MockMultipartFile file = new MockMultipartFile(
            "file", "activities.csv", MediaType.TEXT_PLAIN_VALUE, "csv content".getBytes()
        );

        mvc.perform(multipart("/api/sessions/import").file(file))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.created").value(18))
            .andExpect(jsonPath("$.skippedDuplicates").value(2))
            .andExpect(jsonPath("$.skippedUnknownType").value(1))
            .andExpect(jsonPath("$.unknownTypes[0]").value("Bieganie"))
            .andExpect(jsonPath("$.skippedMalformed").value(0));
    }

    private Session session(Long id, LocalDateTime date) {
        Session s = new Session();
        s.setId(id);
        s.setDate(date);
        s.setType(SessionType.WALKING);
        s.setTitle("Test walk");
        s.setDuration(Duration.ofMinutes(30));
        return s;
    }
}