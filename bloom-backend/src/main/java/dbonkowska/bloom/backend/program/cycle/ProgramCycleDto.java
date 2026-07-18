package dbonkowska.bloom.backend.program.cycle;

import java.time.LocalDate;

public record ProgramCycleDto(Long id, Long programId, String programName, LocalDate startDate, LocalDate endDate) {

    public static ProgramCycleDto from(ProgramCycle cycle) {
        int numDays = cycle.getProgram().getDays().size();
        LocalDate endDate = numDays > 0
            ? cycle.getStartDate().plusDays(numDays - 1)
            : cycle.getStartDate();
        return new ProgramCycleDto(cycle.getId(), cycle.getProgram().getId(), cycle.getProgram().getName(), cycle.getStartDate(), endDate);
    }
}