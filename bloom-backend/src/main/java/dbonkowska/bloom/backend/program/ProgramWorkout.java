package dbonkowska.bloom.backend.program;

import dbonkowska.bloom.backend.workout.Workout;
import jakarta.persistence.*;

@Entity
public class ProgramWorkout {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "program_day_id")
    private ProgramDay programDay;

    @ManyToOne
    @JoinColumn(name = "workout_id")
    private Workout workout;

    @Column(name = "sort_order", nullable = false)
    private Integer order;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public ProgramDay getProgramDay() { return programDay; }
    public void setProgramDay(ProgramDay programDay) { this.programDay = programDay; }
    public Workout getWorkout() { return workout; }
    public void setWorkout(Workout workout) { this.workout = workout; }
    public Integer getOrder() { return order; }
    public void setOrder(Integer order) { this.order = order; }
}