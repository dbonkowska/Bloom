package dbonkowska.bloom.backend.session;

import dbonkowska.bloom.backend.session.garmin.GarminImportResultDto;
import dbonkowska.bloom.backend.session.garmin.GarminImportService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/sessions")
public class SessionController {

    private final SessionRepository repository;
    private final GarminImportService importService;

    public SessionController(SessionRepository repository, GarminImportService importService) {
        this.repository = repository;
        this.importService = importService;
    }

    @GetMapping
    public List<SessionDto> list(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must not be after to");
        }
        List<Session> sessions = (from != null && to != null)
            ? repository.findByDateBetween(from, to)
            : repository.findAll();
        return sessions.stream().map(this::toDto).toList();
    }

    @PostMapping("/import")
    public GarminImportResultDto importCsv(@RequestParam("file") MultipartFile file) throws IOException {
        return importService.importCsv(file.getInputStream());
    }

    private SessionDto toDto(Session s) {
        return new SessionDto(
            s.getId(),
            s.getDate(),
            s.getType(),
            s.getTitle(),
            Math.toIntExact(s.getDuration().getSeconds()),
            s.getDistance(),
            s.getAvgHeartRate(),
            s.getMaxHeartRate(),
            s.getCalories()
        );
    }
}