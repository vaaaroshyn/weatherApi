package com.spring.myapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

@Schema(description = "Sensor registration payload")
public class SensorDTO {
    @Schema(description = "Unique sensor name", example = "sensor-1", minLength = 3, maxLength = 30)
    @NotEmpty(message = "The name should not be blank!")
    @Size(min = 3, max = 30, message = "The name of the sensor must be between 3 and 30 characters!")
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
