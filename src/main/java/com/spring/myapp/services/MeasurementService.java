package com.spring.myapp.services;

import com.spring.myapp.exceptions.SensorNotFoundException;
import com.spring.myapp.models.Measurement;
import com.spring.myapp.models.Sensor;
import com.spring.myapp.repositories.MeasurementRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@Transactional(readOnly = true)
public class MeasurementService {

    private final MeasurementRepository measurementRepository;
    private final SensorService sensorService;

    public MeasurementService(MeasurementRepository measurementRepository,
                              SensorService sensorService) {
        this.measurementRepository = measurementRepository;
        this.sensorService = sensorService;
    }

    public Page<Measurement> findAll(Pageable pageable) {
        return measurementRepository.findAll(pageable);
    }

    public long countRainyMeasurements() {
        return measurementRepository.countByRainingTrue();
    }

    @Transactional
    public void addMeasurement(Measurement measurement) {
        Sensor sensor = findRegisteredSensor(measurement);
        measurement.setSensor(sensor);
        measurement.setMeasurementDateTime(LocalDateTime.now());
        measurementRepository.save(measurement);
    }

    private Sensor findRegisteredSensor(Measurement measurement) {
        if (measurement.getSensor() == null || measurement.getSensor().getName() == null) {
            throw new SensorNotFoundException();
        }

        return sensorService.findByName(measurement.getSensor().getName())
                .orElseThrow(SensorNotFoundException::new);
    }
}
