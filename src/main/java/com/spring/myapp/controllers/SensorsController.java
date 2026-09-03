package com.spring.myapp.controllers;

import com.spring.myapp.dto.SensorDTO;
import com.spring.myapp.dto.ApiError;
import com.spring.myapp.models.Sensor;
import com.spring.myapp.services.SensorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/sensors")
@Tag(name = "Sensors", description = "Registered weather sensors")
public class SensorsController {

    private final SensorService sensorService;
    private final ModelMapper modelMapper;

    public SensorsController(SensorService sensorService, ModelMapper modelMapper) {
        this.sensorService = sensorService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    @Operation(summary = "Register a sensor", description = "Registers a new sensor by unique name.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Sensor registered"),
            @ApiResponse(responseCode = "400", description = "Invalid sensor request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Sensor name already exists",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> registration(@RequestBody @Valid SensorDTO sensorDTO) {
        Sensor sensorToAdd = convertToSensor(sensorDTO);
        sensorService.register(sensorToAdd);
        return ResponseEntity.created(URI.create("/api/v1/sensors")).build();
    }

    private Sensor convertToSensor(SensorDTO sensorDTO) {
        return modelMapper.map(sensorDTO, Sensor.class);
    }
}
