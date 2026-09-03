package com.spring.myapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "Stored weather measurement")
public class MeasurementResponse {
    @Schema(example = "1")
    private Integer id;
    @Schema(description = "Temperature value in Celsius", example = "23.4")
    private Double value;
    @Schema(example = "false")
    private Boolean raining;
    @Schema(description = "Server-side measurement timestamp", example = "2026-09-03T09:15:30")
    private LocalDateTime measuredAt;
    @Schema(description = "Sensor that produced the measurement")
    private SensorResponse sensor;

    public MeasurementResponse(Integer id, Double value, Boolean raining, LocalDateTime measuredAt,
                               SensorResponse sensor) {
        this.id = id;
        this.value = value;
        this.raining = raining;
        this.measuredAt = measuredAt;
        this.sensor = sensor;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

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

    public LocalDateTime getMeasuredAt() {
        return measuredAt;
    }

    public void setMeasuredAt(LocalDateTime measuredAt) {
        this.measuredAt = measuredAt;
    }

    public SensorResponse getSensor() {
        return sensor;
    }

    public void setSensor(SensorResponse sensor) {
        this.sensor = sensor;
    }
}
