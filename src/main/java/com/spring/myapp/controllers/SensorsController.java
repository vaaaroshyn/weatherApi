package com.spring.myapp.controllers;

import com.spring.myapp.dto.SensorDTO;
import com.spring.myapp.models.Sensor;
import com.spring.myapp.services.SensorService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/sensors")
public class SensorsController {

    private final SensorService sensorService;
    private final ModelMapper modelMapper;

    public SensorsController(SensorService sensorService, ModelMapper modelMapper) {
        this.sensorService = sensorService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    public ResponseEntity<Void> registration(@RequestBody @Valid SensorDTO sensorDTO) {
        Sensor sensorToAdd = convertToSensor(sensorDTO);
        sensorService.register(sensorToAdd);
        return ResponseEntity.created(URI.create("/api/v1/sensors")).build();
    }

    private Sensor convertToSensor(SensorDTO sensorDTO) {
        return modelMapper.map(sensorDTO, Sensor.class);
    }
}
