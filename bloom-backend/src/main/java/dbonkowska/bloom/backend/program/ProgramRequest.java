package dbonkowska.bloom.backend.program;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record ProgramRequest(@NotBlank String name, String description, List<ProgramDayRequest> days) {}