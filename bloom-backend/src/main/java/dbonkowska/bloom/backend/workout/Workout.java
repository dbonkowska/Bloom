package dbonkowska.bloom.backend.workout;

import dbonkowska.bloom.backend.workout.author.Author;
import jakarta.persistence.*;

import java.util.List;

@Entity
public class Workout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String youtubeUrl;

    private Integer durationMinutes;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "workout_muscle_group", joinColumns = @JoinColumn(name = "workout_id"))
    @Column(name = "muscle_group")
    private List<MuscleGroup> muscleGroups;

    private String notes;

    @ManyToOne(optional = false)
    @JoinColumn(name = "author_id", nullable = false)
    private Author author;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getYoutubeUrl() { return youtubeUrl; }
    public void setYoutubeUrl(String youtubeUrl) { this.youtubeUrl = youtubeUrl; }
    public Integer getDurationMinutes() { return durationMinutes; }
    public void setDurationMinutes(Integer durationMinutes) { this.durationMinutes = durationMinutes; }
    public List<MuscleGroup> getMuscleGroups() { return muscleGroups; }
    public void setMuscleGroups(List<MuscleGroup> muscleGroups) { this.muscleGroups = muscleGroups; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Author getAuthor() { return author; }
    public void setAuthor(Author author) { this.author = author; }
}