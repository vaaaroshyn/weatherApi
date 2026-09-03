package com.spring.myapp.controllers;

import com.spring.myapp.dto.MeasurementDTO;
import com.spring.myapp.dto.MeasurementsResponse;
import com.spring.myapp.models.Measurement;
import com.spring.myapp.services.MeasurementService;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.stream.Collectors;


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
    public MeasurementsResponse getMeasurements() {
        return new MeasurementsResponse(measurementService.findAll().stream().map(this::convertToMeasurementDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/rainy-days/count")
    public Long getRainyDaysCount() {
        return measurementService.findAll().stream().filter(Measurement::isRaining).count();
    }

    private Measurement convertToMeasurement(MeasurementDTO measurementDTO) {
        return modelMapper.map(measurementDTO, Measurement.class);
    }

    private MeasurementDTO convertToMeasurementDTO(Measurement measurement) {
        return modelMapper.map(measurement, MeasurementDTO.class);
    }

}
