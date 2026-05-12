package dbonkowska.bloom.backend.activity;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityRepository repository;

    public ActivityController(ActivityRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<ActivityDto> list(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        List<Activity> activities = (from != null && to != null)
            ? repository.findByDateBetween(from, to)
            : repository.findAll();
        return activities.stream().map(this::toDto).toList();
    }

    private ActivityDto toDto(Activity a) {
        return new ActivityDto(
            a.getId(),
            a.getDate(),
            a.getType(),
            a.getTitle(),
            Math.toIntExact(a.getDuration().getSeconds()),
            a.getDistance(),
            a.getAvgHeartRate(),
            a.getMaxHeartRate(),
            a.getCalories()
        );
    }
}
