package dbonkowska.bloom.backend.program;

import java.util.List;

public record ProgramDto(Long id, String name, String description, List<ProgramDayDto> days) {}