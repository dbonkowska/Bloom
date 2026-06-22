package dbonkowska.bloom.backend.bodymeasurement;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BodyMeasurementController.class)
class BodyMeasurementControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private BodyMeasurementRepository repository;

    @Test
    void listMeasurements_noParams_returnsAll() throws Exception {
        when(repository.findAllByOrderByDateDesc()).thenReturn(List.of(
            measurement(1L, LocalDate.of(2026, 1, 20)),
            measurement(2L, LocalDate.of(2026, 1, 10))
        ));

        mvc.perform(get("/api/body-measurements"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].date").value("2026-01-20"))
            .andExpect(jsonPath("$[0].weight").value(72.5));
    }

    @Test
    void listMeasurements_withDateRange_returnsFiltered() throws Exception {
        when(repository.findByDateBetweenOrderByDateDesc(
            eq(LocalDate.of(2026, 1, 1)),
            eq(LocalDate.of(2026, 1, 31))
        )).thenReturn(List.of(measurement(1L, LocalDate.of(2026, 1, 20))));

        mvc.perform(get("/api/body-measurements")
                .param("from", "2026-01-01")
                .param("to", "2026-01-31"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void listMeasurements_fromAfterTo_returns400() throws Exception {
        mvc.perform(get("/api/body-measurements")
                .param("from", "2026-01-31")
                .param("to", "2026-01-01"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getMeasurement_existingId_returnsRecord() throws Exception {
        when(repository.findById(1L)).thenReturn(Optional.of(measurement(1L, LocalDate.of(2026, 1, 20))));

        mvc.perform(get("/api/body-measurements/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.date").value("2026-01-20"));
    }

    @Test
    void getMeasurement_unknownId_returns404() throws Exception {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        mvc.perform(get("/api/body-measurements/99"))
            .andExpect(status().isNotFound());
    }

    @Test
    void createMeasurement_validBody_returns201WithSavedRecord() throws Exception {
        BodyMeasurement saved = measurement(1L, LocalDate.of(2026, 1, 20));
        when(repository.save(any())).thenReturn(saved);

        mvc.perform(post("/api/body-measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "date": "2026-01-20",
                        "weight": 72.5,
                        "bodyFatPct": 18.3
                    }
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1))
            .andExpect(jsonPath("$.weight").value(72.5));
    }

    @Test
    void createMeasurement_missingDate_returns400() throws Exception {
        mvc.perform(post("/api/body-measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "weight": 72.5
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createMeasurement_negativeWeight_returns400() throws Exception {
        mvc.perform(post("/api/body-measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "date": "2026-01-20",
                        "weight": -1.0
                    }
                    """))
            .andExpect(status().isBadRequest());
    }

    @Test
    void createMeasurement_duplicateDate_returns409() throws Exception {
        when(repository.save(any())).thenThrow(new DataIntegrityViolationException("unique constraint"));

        mvc.perform(post("/api/body-measurements")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "date": "2026-01-20",
                        "weight": 72.5
                    }
                    """))
            .andExpect(status().isConflict());
    }

    @Test
    void updateMeasurement_validBody_returnsUpdatedRecord() throws Exception {
        BodyMeasurement existing = measurement(1L, LocalDate.of(2026, 1, 20));
        BodyMeasurement updated = measurement(1L, LocalDate.of(2026, 1, 20));
        updated.setWeight(73.0);
        when(repository.findById(1L)).thenReturn(Optional.of(existing));
        when(repository.save(any())).thenReturn(updated);

        mvc.perform(put("/api/body-measurements/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "date": "2026-01-20",
                        "weight": 73.0
                    }
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.weight").value(73.0));
    }

    @Test
    void updateMeasurement_unknownId_returns404() throws Exception {
        when(repository.findById(99L)).thenReturn(Optional.empty());

        mvc.perform(put("/api/body-measurements/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {
                        "date": "2026-01-20",
                        "weight": 72.5
                    }
                    """))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteMeasurement_existingId_returns204() throws Exception {
        when(repository.existsById(1L)).thenReturn(true);

        mvc.perform(delete("/api/body-measurements/1"))
            .andExpect(status().isNoContent());
    }

    private BodyMeasurement measurement(Long id, LocalDate date) {
        BodyMeasurement m = new BodyMeasurement();
        m.setId(id);
        m.setDate(date);
        m.setWeight(72.5);
        return m;
    }
}
