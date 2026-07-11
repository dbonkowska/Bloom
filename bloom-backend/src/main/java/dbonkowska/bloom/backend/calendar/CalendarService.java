package dbonkowska.bloom.backend.calendar;

import dbonkowska.bloom.backend.program.session.PlannedSession;
import dbonkowska.bloom.backend.program.session.PlannedSessionDto;
import dbonkowska.bloom.backend.program.session.PlannedSessionRepository;
import dbonkowska.bloom.backend.session.Session;
import dbonkowska.bloom.backend.session.SessionDto;
import dbonkowska.bloom.backend.session.SessionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;

@Service
public class CalendarService {

    private final SessionRepository sessionRepository;
    private final PlannedSessionRepository plannedSessionRepository;

    public CalendarService(SessionRepository sessionRepository, PlannedSessionRepository plannedSessionRepository) {
        this.sessionRepository = sessionRepository;
        this.plannedSessionRepository = plannedSessionRepository;
    }

    public List<CalendarDayDto> findCalendar(LocalDate from, LocalDate to) {
        if (from != null && to != null && from.isAfter(to)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must not be after to");
        }

        List<Session> sessions = findActuals(from, to);
        List<PlannedSession> plannedSessions = findPlanned(from, to);

        TreeMap<LocalDate, Bucket> buckets = new TreeMap<>();
        for (Session s : sessions) {
            buckets.computeIfAbsent(s.getDate().toLocalDate(), d -> new Bucket()).actual.add(s);
        }
        for (PlannedSession p : plannedSessions) {
            buckets.computeIfAbsent(p.getDate(), d -> new Bucket()).planned.add(p);
        }

        List<CalendarDayDto> result = new ArrayList<>();
        for (var entry : buckets.entrySet()) {
            Bucket bucket = entry.getValue();
            bucket.actual.sort(Comparator.comparing(Session::getDate));
            bucket.planned.sort(Comparator.comparing(PlannedSession::getId));
            List<SessionDto> actual = bucket.actual.stream().map(SessionDto::from).toList();
            List<PlannedSessionDto> planned = bucket.planned.stream().map(PlannedSessionDto::from).toList();
            result.add(new CalendarDayDto(entry.getKey(), actual, planned));
        }
        return result;
    }

    private List<Session> findActuals(LocalDate from, LocalDate to) {
        LocalDateTime lower = from != null ? from.atStartOfDay() : null;         // inclusive
        LocalDateTime upper = to != null ? to.plusDays(1).atStartOfDay() : null; // exclusive
        if (lower != null && upper != null) return sessionRepository.findByDateGreaterThanEqualAndDateLessThan(lower, upper);
        if (lower != null) return sessionRepository.findByDateGreaterThanEqual(lower);
        if (upper != null) return sessionRepository.findByDateLessThan(upper);
        return sessionRepository.findAll();
    }

    private List<PlannedSession> findPlanned(LocalDate from, LocalDate to) {
        if (from != null && to != null) return plannedSessionRepository.findByDateBetween(from, to);
        if (from != null) return plannedSessionRepository.findByDateGreaterThanEqual(from);
        if (to != null) return plannedSessionRepository.findByDateLessThanEqual(to);
        return plannedSessionRepository.findAll();
    }

    private static class Bucket {
        final List<Session> actual = new ArrayList<>();
        final List<PlannedSession> planned = new ArrayList<>();
    }
}