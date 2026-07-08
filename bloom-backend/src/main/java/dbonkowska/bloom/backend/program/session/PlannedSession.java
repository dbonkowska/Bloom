package dbonkowska.bloom.backend.program.session;

import dbonkowska.bloom.backend.program.workout.ProgramWorkout;
import dbonkowska.bloom.backend.program.cycle.ProgramCycle;
import dbonkowska.bloom.backend.workout.Workout;
import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
public class PlannedSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate date;

    @ManyToOne(optional = false)
    @JoinColumn(name = "workout_id")
    private Workout workout;

    @ManyToOne
    @JoinColumn(name = "program_cycle_id")
    private ProgramCycle programCycle;

    @ManyToOne
    @JoinColumn(name = "program_workout_id")
    private ProgramWorkout programWorkout;

    @Column(nullable = false)
    private boolean completed;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public Workout getWorkout() { return workout; }
    public void setWorkout(Workout workout) { this.workout = workout; }
    public ProgramCycle getProgramCycle() { return programCycle; }
    public void setProgramCycle(ProgramCycle programCycle) { this.programCycle = programCycle; }
    public ProgramWorkout getProgramWorkout() { return programWorkout; }
    public void setProgramWorkout(ProgramWorkout programWorkout) { this.programWorkout = programWorkout; }
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}