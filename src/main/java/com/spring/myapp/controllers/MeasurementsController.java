package com.spring.myapp.controllers;

import com.spring.myapp.dto.MeasurementDTO;
import com.spring.myapp.dto.MeasurementResponse;
import com.spring.myapp.dto.PageResponse;
import com.spring.myapp.dto.SensorResponse;
import com.spring.myapp.models.Measurement;
import com.spring.myapp.models.Sensor;
import com.spring.myapp.services.MeasurementService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;


@RestController
@RequestMapping("/api/v1/measurements")
public class MeasurementsController {

    private final MeasurementService measurementService;
    private final ModelMapper modelMapper;

    public MeasurementsController(MeasurementService measurementService,
                                  ModelMapper modelMapper) {
        this.measurementService = measurementService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    public ResponseEntity<Void> add(@RequestBody @Valid MeasurementDTO measurementDTO) {
        Measurement measurementToAdd = convertToMeasurement(measurementDTO);
        measurementService.addMeasurement(measurementToAdd);
        return ResponseEntity.created(URI.create("/api/v1/measurements")).build();
    }

    @GetMapping()
    public PageResponse<MeasurementResponse> getMeasurements(Pageable pageable) {
        Page<MeasurementResponse> measurements = measurementService.findAll(pageable)
                .map(this::convertToMeasurementResponse);

        return PageResponse.from(measurements);
    }

    @GetMapping("/rainy-days/count")
    public Long getRainyDaysCount() {
        return measurementService.countRainyMeasurements();
    }

    private Measurement convertToMeasurement(MeasurementDTO measurementDTO) {
        return modelMapper.map(measurementDTO, Measurement.class);
    }

    private MeasurementResponse convertToMeasurementResponse(Measurement measurement) {
        return new MeasurementResponse(
                measurement.getId(),
                measurement.getValue(),
                measurement.isRaining(),
                measurement.getMeasurementDateTime(),
                convertToSensorResponse(measurement.getSensor())
        );
    }

    private SensorResponse convertToSensorResponse(Sensor sensor) {
        return new SensorResponse(sensor.getId(), sensor.getName());
    }

}
