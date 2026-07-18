package dbonkowska.bloom.backend.program;

import dbonkowska.bloom.backend.program.day.ProgramDayDto;
import java.util.List;

public record ProgramDto(Long id, String name, String description, List<ProgramDayDto> days) {

    public static ProgramDto from(Program program) {
        return new ProgramDto(
            program.getId(),
            program.getName(),
            program.getDescription(),
            program.getDays().stream().map(ProgramDayDto::from).toList()
        );
    }
}