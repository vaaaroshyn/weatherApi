package com.spring.myapp.controllers;

import com.spring.myapp.dto.SensorDTO;
import com.spring.myapp.exceptions.SensorAlreadyExistsException;
import com.spring.myapp.models.Sensor;
import com.spring.myapp.services.SensorService;
import com.spring.myapp.util.MeasurementErrorResponse;
import com.spring.myapp.util.MeasurementException;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import static com.spring.myapp.util.ErrorsUtil.returnErrorsToClient;


@RestController
@RequestMapping("/sensors")
public class SensorsController {

    private final SensorService sensorService;
    private final ModelMapper modelMapper;

    public SensorsController(SensorService sensorService, ModelMapper modelMapper) {
        this.sensorService = sensorService;
        this.modelMapper = modelMapper;
    }

    @PostMapping("/registration")
    public ResponseEntity<HttpStatus> registration(@RequestBody @Valid SensorDTO sensorDTO,
                                                   BindingResult bindingResult) {
        if (bindingResult.hasErrors())
            returnErrorsToClient(bindingResult);

        Sensor sensorToAdd = convertToSensor(sensorDTO);
        sensorService.register(sensorToAdd);
        return ResponseEntity.ok(HttpStatus.OK);
    }

    @ExceptionHandler({MeasurementException.class, SensorAlreadyExistsException.class})
    private ResponseEntity<MeasurementErrorResponse> handleException(RuntimeException e) {
        MeasurementErrorResponse response = new MeasurementErrorResponse(
                e.getMessage(),
                System.currentTimeMillis()
        );

        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    private Sensor convertToSensor(SensorDTO sensorDTO) {
        return modelMapper.map(sensorDTO, Sensor.class);
    }
}
