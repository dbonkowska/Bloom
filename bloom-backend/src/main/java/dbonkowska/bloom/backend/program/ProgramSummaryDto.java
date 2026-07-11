package dbonkowska.bloom.backend.program;

public record ProgramSummaryDto(Long id, String name, String description, int totalDays) {

    public static ProgramSummaryDto from(Program program) {
        return new ProgramSummaryDto(program.getId(), program.getName(), program.getDescription(), program.getDays().size());
    }
}