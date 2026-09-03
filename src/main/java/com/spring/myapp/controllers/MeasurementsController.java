package com.spring.myapp.controllers;

import com.spring.myapp.dto.ApiError;
import com.spring.myapp.dto.MeasurementDTO;
import com.spring.myapp.dto.MeasurementResponse;
import com.spring.myapp.dto.PageResponse;
import com.spring.myapp.dto.SensorResponse;
import com.spring.myapp.models.Measurement;
import com.spring.myapp.models.Sensor;
import com.spring.myapp.services.MeasurementService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;


@RestController
@RequestMapping("/api/v1/measurements")
@Tag(name = "Measurements", description = "Weather measurements submitted by registered sensors")
public class MeasurementsController {

    private final MeasurementService measurementService;
    private final ModelMapper modelMapper;

    public MeasurementsController(MeasurementService measurementService,
                                  ModelMapper modelMapper) {
        this.measurementService = measurementService;
        this.modelMapper = modelMapper;
    }

    @PostMapping
    @Operation(summary = "Add a measurement", description = "Stores a weather measurement for an existing sensor.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Measurement stored"),
            @ApiResponse(responseCode = "400", description = "Invalid measurement request",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Sensor was not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public ResponseEntity<Void> add(@RequestBody @Valid MeasurementDTO measurementDTO) {
        Measurement measurementToAdd = convertToMeasurement(measurementDTO);
        measurementService.addMeasurement(measurementToAdd);
        return ResponseEntity.created(URI.create("/api/v1/measurements")).build();
    }

    @GetMapping()
    @Operation(summary = "Get measurements", description = "Returns measurements as a pageable response.")
    @ApiResponse(responseCode = "200", description = "Measurements page")
    public PageResponse<MeasurementResponse> getMeasurements(@ParameterObject Pageable pageable) {
        Page<MeasurementResponse> measurements = measurementService.findAll(pageable)
                .map(this::convertToMeasurementResponse);

        return PageResponse.from(measurements);
    }

    @GetMapping("/rainy-days/count")
    @Operation(summary = "Count rainy measurements", description = "Returns the number of stored measurements marked as raining.")
    @ApiResponse(responseCode = "200", description = "Rainy measurements count")
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
