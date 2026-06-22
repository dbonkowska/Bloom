package dbonkowska.bloom.backend.bodymeasurement;

import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/body-measurements")
public class BodyMeasurementController {

    private final BodyMeasurementRepository repository;

    public BodyMeasurementController(BodyMeasurementRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<BodyMeasurementDto> list(
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        if (from != null && to != null) {
            if (from.isAfter(to)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "from must not be after to");
            }
            return repository.findByDateBetweenOrderByDateDesc(from, to).stream()
                .map(this::toDto).toList();
        }
        return repository.findAllByOrderByDateDesc().stream()
            .map(this::toDto).toList();
    }

    @GetMapping("/{id}")
    public BodyMeasurementDto getById(@PathVariable Long id) {
        return repository.findById(id)
            .map(this::toDto)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BodyMeasurementDto create(@RequestBody @Valid BodyMeasurementDto dto) {
        try {
            return toDto(repository.save(toEntity(dto)));
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A measurement for this date already exists");
        }
    }

    @PutMapping("/{id}")
    public BodyMeasurementDto update(@PathVariable Long id, @RequestBody @Valid BodyMeasurementDto dto) {
        BodyMeasurement entity = repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        entity.setDate(dto.date());
        entity.setWeight(dto.weight());
        entity.setBodyFatPct(dto.bodyFatPct());
        entity.setMuscleMass(dto.muscleMass());
        entity.setWaterPct(dto.waterPct());
        entity.setBonesPct(dto.bonesPct());
        entity.setBmi(dto.bmi());
        entity.setChest(dto.chest());
        entity.setWaist(dto.waist());
        entity.setStomach(dto.stomach());
        entity.setHips(dto.hips());
        entity.setForearmLeft(dto.forearmLeft());
        entity.setForearmRight(dto.forearmRight());
        entity.setArmLeft(dto.armLeft());
        entity.setArmRight(dto.armRight());
        entity.setThighLeft(dto.thighLeft());
        entity.setThighRight(dto.thighRight());
        entity.setCalfLeft(dto.calfLeft());
        entity.setCalfRight(dto.calfRight());
        try {
            return toDto(repository.save(entity));
        } catch (DataIntegrityViolationException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "A measurement for this date already exists");
        }
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        repository.deleteById(id);
    }

    private BodyMeasurementDto toDto(BodyMeasurement m) {
        return new BodyMeasurementDto(
            m.getId(), m.getDate(),
            m.getWeight(), m.getBodyFatPct(), m.getMuscleMass(), m.getWaterPct(), m.getBonesPct(), m.getBmi(),
            m.getChest(), m.getWaist(), m.getStomach(), m.getHips(),
            m.getForearmLeft(), m.getForearmRight(),
            m.getArmLeft(), m.getArmRight(),
            m.getThighLeft(), m.getThighRight(),
            m.getCalfLeft(), m.getCalfRight()
        );
    }

    private BodyMeasurement toEntity(BodyMeasurementDto dto) {
        BodyMeasurement m = new BodyMeasurement();
        m.setDate(dto.date());
        m.setWeight(dto.weight());
        m.setBodyFatPct(dto.bodyFatPct());
        m.setMuscleMass(dto.muscleMass());
        m.setWaterPct(dto.waterPct());
        m.setBonesPct(dto.bonesPct());
        m.setBmi(dto.bmi());
        m.setChest(dto.chest());
        m.setWaist(dto.waist());
        m.setStomach(dto.stomach());
        m.setHips(dto.hips());
        m.setForearmLeft(dto.forearmLeft());
        m.setForearmRight(dto.forearmRight());
        m.setArmLeft(dto.armLeft());
        m.setArmRight(dto.armRight());
        m.setThighLeft(dto.thighLeft());
        m.setThighRight(dto.thighRight());
        m.setCalfLeft(dto.calfLeft());
        m.setCalfRight(dto.calfRight());
        return m;
    }
}
