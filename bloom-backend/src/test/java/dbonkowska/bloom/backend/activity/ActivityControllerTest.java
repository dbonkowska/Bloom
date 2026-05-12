package dbonkowska.bloom.backend.activity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ActivityController.class)
class ActivityControllerTest {

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private ActivityRepository repository;

    @Test
    void getActivities_noParams_returnsAll() throws Exception {
        when(repository.findAll()).thenReturn(List.of(
            activity(1L, LocalDateTime.of(2026, 1, 10, 10, 0))
        ));

        mvc.perform(get("/api/activities"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(1))
            .andExpect(jsonPath("$[0].type").value("WALKING"))
            .andExpect(jsonPath("$[0].durationSeconds").value(1800));
    }

    @Test
    void getActivities_withDateRange_returnsFiltered() throws Exception {
        when(repository.findByDateBetween(
            eq(LocalDateTime.of(2026, 1, 10, 0, 0)),
            eq(LocalDateTime.of(2026, 1, 20, 0, 0))
        )).thenReturn(List.of(
            activity(2L, LocalDateTime.of(2026, 1, 15, 10, 0))
        ));

        mvc.perform(get("/api/activities")
                .param("from", "2026-01-10T00:00:00")
                .param("to", "2026-01-20T00:00:00"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1))
            .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    void getActivities_withOnlyOneParam_returnsAll() throws Exception {
        when(repository.findAll()).thenReturn(List.of(
            activity(1L, LocalDateTime.of(2026, 1, 10, 10, 0))
        ));

        mvc.perform(get("/api/activities").param("from", "2026-01-10T00:00:00"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(1));
    }

    private Activity activity(Long id, LocalDateTime date) {
        Activity a = new Activity();
        a.setId(id);
        a.setDate(date);
        a.setType(ActivityType.WALKING);
        a.setTitle("Test walk");
        a.setDuration(Duration.ofMinutes(30));
        return a;
    }
}
