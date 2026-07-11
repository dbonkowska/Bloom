package dbonkowska.bloom.backend.calendar;

import dbonkowska.bloom.backend.program.session.PlannedSessionDto;
import dbonkowska.bloom.backend.session.SessionDto;

import java.time.LocalDate;
import java.util.List;

public record CalendarDayDto(LocalDate date, List<SessionDto> actual, List<PlannedSessionDto> planned) {}