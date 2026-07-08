package dbonkowska.bloom.backend.program.day;

import dbonkowska.bloom.backend.program.Program;
import dbonkowska.bloom.backend.program.workout.ProgramWorkout;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
public class ProgramDay {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "program_id")
    private Program program;

    @Column(name = "day_number", nullable = false)
    private Integer dayNumber;

    @OneToMany(mappedBy = "programDay", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<ProgramWorkout> workouts = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Program getProgram() { return program; }
    public void setProgram(Program program) { this.program = program; }
    public Integer getDayNumber() { return dayNumber; }
    public void setDayNumber(Integer dayNumber) { this.dayNumber = dayNumber; }
    public List<ProgramWorkout> getWorkouts() { return workouts; }
    public void setWorkouts(List<ProgramWorkout> workouts) { this.workouts = workouts; }
}