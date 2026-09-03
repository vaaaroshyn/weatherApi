package com.spring.myapp.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Registered sensor summary")
public class SensorResponse {
    @Schema(example = "1")
    private Integer id;
    @Schema(example = "sensor-1")
    private String name;

    public SensorResponse(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
