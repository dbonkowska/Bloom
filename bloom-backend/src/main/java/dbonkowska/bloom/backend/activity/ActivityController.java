package dbonkowska.bloom.backend.activity;

import dbonkowska.bloom.backend.activity.garmin.GarminImportResultDto;
import dbonkowska.bloom.backend.activity.garmin.GarminImportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/activities")
public class ActivityController {

    private final ActivityRepository repository;
    private final GarminImportService importService;

    public ActivityController(ActivityRepository repository, GarminImportService importService) {
        this.repository = repository;
        this.importService = importService;
    }

    @GetMapping
    public List<ActivityDto> list(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must not be after to");
        }
        List<Activity> activities = (from != null && to != null)
            ? repository.findByDateBetween(from, to)
            : repository.findAll();
        return activities.stream().map(this::toDto).toList();
    }

    @PostMapping("/import")
    public GarminImportResultDto importCsv(@RequestParam("file") MultipartFile file) throws IOException {
        return importService.importCsv(file.getInputStream());
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
