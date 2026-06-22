package dbonkowska.bloom.backend.bodymeasurement;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDate;

@Entity
public class BodyMeasurement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate date;

    private Double weight;
    private Double bodyFatPct;
    private Double muscleMass;
    private Double waterPct;
    private Double bonesPct;
    private Double bmi;
    private Double chest;
    private Double waist;
    private Double stomach;
    private Double hips;
    private Double forearmLeft;
    private Double forearmRight;
    private Double armLeft;
    private Double armRight;
    private Double thighLeft;
    private Double thighRight;
    private Double calfLeft;
    private Double calfRight;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public Double getWeight() { return weight; }
    public void setWeight(Double weight) { this.weight = weight; }
    public Double getBodyFatPct() { return bodyFatPct; }
    public void setBodyFatPct(Double bodyFatPct) { this.bodyFatPct = bodyFatPct; }
    public Double getMuscleMass() { return muscleMass; }
    public void setMuscleMass(Double muscleMass) { this.muscleMass = muscleMass; }
    public Double getWaterPct() { return waterPct; }
    public void setWaterPct(Double waterPct) { this.waterPct = waterPct; }
    public Double getBonesPct() { return bonesPct; }
    public void setBonesPct(Double bonesPct) { this.bonesPct = bonesPct; }
    public Double getBmi() { return bmi; }
    public void setBmi(Double bmi) { this.bmi = bmi; }
    public Double getChest() { return chest; }
    public void setChest(Double chest) { this.chest = chest; }
    public Double getWaist() { return waist; }
    public void setWaist(Double waist) { this.waist = waist; }
    public Double getStomach() { return stomach; }
    public void setStomach(Double stomach) { this.stomach = stomach; }
    public Double getHips() { return hips; }
    public void setHips(Double hips) { this.hips = hips; }
    public Double getForearmLeft() { return forearmLeft; }
    public void setForearmLeft(Double forearmLeft) { this.forearmLeft = forearmLeft; }
    public Double getForearmRight() { return forearmRight; }
    public void setForearmRight(Double forearmRight) { this.forearmRight = forearmRight; }
    public Double getArmLeft() { return armLeft; }
    public void setArmLeft(Double armLeft) { this.armLeft = armLeft; }
    public Double getArmRight() { return armRight; }
    public void setArmRight(Double armRight) { this.armRight = armRight; }
    public Double getThighLeft() { return thighLeft; }
    public void setThighLeft(Double thighLeft) { this.thighLeft = thighLeft; }
    public Double getThighRight() { return thighRight; }
    public void setThighRight(Double thighRight) { this.thighRight = thighRight; }
    public Double getCalfLeft() { return calfLeft; }
    public void setCalfLeft(Double calfLeft) { this.calfLeft = calfLeft; }
    public Double getCalfRight() { return calfRight; }
    public void setCalfRight(Double calfRight) { this.calfRight = calfRight; }
}
