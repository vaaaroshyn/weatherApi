package com.spring.myapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Schema(description = "Weather measurement payload")
public class MeasurementDTO {
    @Schema(description = "Temperature value in Celsius", example = "23.4", minimum = "-100", maximum = "100")
    @NotNull
    @Min(-100)
    @Max(100)
    private Double value;

    @Schema(description = "Whether it was raining during measurement", example = "false")
    @NotNull
    private Boolean raining;

    @Schema(description = "Registered sensor that produced this measurement")
    @NotNull
    @Valid
    private SensorDTO sensor;

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }

    public Boolean getRaining() {
        return raining;
    }

    public void setRaining(Boolean raining) {
        this.raining = raining;
    }

    public SensorDTO getSensor() {
        return sensor;
    }

    public void setSensor(SensorDTO sensor) {
        this.sensor = sensor;
    }
}
