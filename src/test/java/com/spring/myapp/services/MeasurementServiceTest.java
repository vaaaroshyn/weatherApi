package com.spring.myapp.services;

import com.spring.myapp.exceptions.SensorNotFoundException;
import com.spring.myapp.models.Measurement;
import com.spring.myapp.models.Sensor;
import com.spring.myapp.repositories.MeasurementRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MeasurementServiceTest {

    @Mock
    private MeasurementRepository measurementRepository;

    @Mock
    private SensorService sensorService;

    @InjectMocks
    private MeasurementService measurementService;

    @Test
    void findAllUsesRepositoryPageQuery() {
        Pageable pageable = PageRequest.of(0, 20);
        when(measurementRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(), pageable, 0));

        measurementService.findAll(pageable);

        verify(measurementRepository).findAll(pageable);
    }

    @Test
    void countRainyMeasurementsUsesDatabaseCountQuery() {
        when(measurementRepository.countByRainingTrue()).thenReturn(3L);

        long count = measurementService.countRainyMeasurements();

        assertEquals(3L, count);
        verify(measurementRepository).countByRainingTrue();
    }

    @Test
    void addMeasurementUsesRegisteredSensorAndSetsMeasuredAt() {
        Sensor requestSensor = sensor("outside");
        Sensor registeredSensor = sensor("outside");
        Measurement measurement = measurement(requestSensor);
        when(sensorService.findByName("outside")).thenReturn(Optional.of(registeredSensor));

        measurementService.addMeasurement(measurement);

        assertSame(registeredSensor, measurement.getSensor());
        assertNotNull(measurement.getMeasurementDateTime());
        verify(measurementRepository).save(measurement);
    }

    @Test
    void addMeasurementRejectsUnknownSensor() {
        Measurement measurement = measurement(sensor("unknown"));
        when(sensorService.findByName("unknown")).thenReturn(Optional.empty());

        assertThrows(SensorNotFoundException.class, () -> measurementService.addMeasurement(measurement));

        verify(measurementRepository, never()).save(measurement);
    }

    private Measurement measurement(Sensor sensor) {
        Measurement measurement = new Measurement();
        measurement.setSensor(sensor);
        measurement.setValue(12.5);
        measurement.setRaining(false);
        return measurement;
    }

    private Sensor sensor(String name) {
        Sensor sensor = new Sensor();
        sensor.setName(name);
        return sensor;
    }
}
