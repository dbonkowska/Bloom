package dbonkowska.bloom.backend.workout.author;

import dbonkowska.bloom.backend.workout.MuscleGroup;
import dbonkowska.bloom.backend.workout.Workout;
import dbonkowska.bloom.backend.workout.WorkoutRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.context.ImportTestcontainers;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ImportTestcontainers
class AuthorIntegrationTest {

    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:17");

    @Autowired MockMvc mvc;
    @Autowired AuthorRepository authorRepository;
    @Autowired WorkoutRepository workoutRepository;

    @BeforeEach
    void setUp() {
        workoutRepository.deleteAll();
        authorRepository.deleteAll();
    }

    @Test
    void createAuthor_validBody_returns201WithDto() throws Exception {
        mvc.perform(post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Caroline Girvan"}
                    """))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").isNumber())
            .andExpect(jsonPath("$.name").value("Caroline Girvan"));
    }

    @Test
    void createAuthor_missingName_returns400() throws Exception {
        mvc.perform(post("/api/authors")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void listAuthors_returnsAll() throws Exception {
        Author a1 = author("Caroline Girvan");
        Author a2 = author("Jeff Nippard");
        authorRepository.saveAll(List.of(a1, a2));

        mvc.perform(get("/api/authors"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void getAuthor_existingId_returns200WithDto() throws Exception {
        Author saved = authorRepository.save(author("Caroline Girvan"));

        mvc.perform(get("/api/authors/" + saved.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(saved.getId()))
            .andExpect(jsonPath("$.name").value("Caroline Girvan"));
    }

    @Test
    void getAuthor_unknownId_returns404() throws Exception {
        mvc.perform(get("/api/authors/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void updateAuthor_existingId_returns200WithUpdatedName() throws Exception {
        Author saved = authorRepository.save(author("Caroline Girvan"));

        mvc.perform(put("/api/authors/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Updated Name"}
                    """))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @Test
    void updateAuthor_missingName_returns400() throws Exception {
        Author saved = authorRepository.save(author("Caroline Girvan"));

        mvc.perform(put("/api/authors/" + saved.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest());
    }

    @Test
    void updateAuthor_unknownId_returns404() throws Exception {
        mvc.perform(put("/api/authors/999")
                .contentType(MediaType.APPLICATION_JSON)
                .content("""
                    {"name":"Updated Name"}
                    """))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteAuthor_existingId_returns204() throws Exception {
        Author saved = authorRepository.save(author("Caroline Girvan"));

        mvc.perform(delete("/api/authors/" + saved.getId()))
            .andExpect(status().isNoContent());
    }

    @Test
    void deleteAuthor_unknownId_returns404() throws Exception {
        mvc.perform(delete("/api/authors/999"))
            .andExpect(status().isNotFound());
    }

    @Test
    void deleteAuthor_withAssociatedWorkouts_returns409() throws Exception {
        Author saved = authorRepository.save(author("Caroline Girvan"));
        workoutRepository.save(workout("EPIC Day 1", saved));

        mvc.perform(delete("/api/authors/" + saved.getId()))
            .andExpect(status().isConflict());
    }

    private Author author(String name) {
        Author a = new Author();
        a.setName(name);
        return a;
    }

    private Workout workout(String name, Author author) {
        Workout w = new Workout();
        w.setName(name);
        w.setMuscleGroups(List.of(MuscleGroup.LEGS));
        w.setAuthor(author);
        return w;
    }
}