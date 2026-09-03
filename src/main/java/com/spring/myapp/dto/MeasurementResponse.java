package com.spring.myapp.dto;

import java.time.LocalDateTime;

public class MeasurementResponse {
    private Integer id;
    private Double value;
    private Boolean raining;
    private LocalDateTime measuredAt;
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
