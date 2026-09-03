package com.spring.myapp.services;

import com.spring.myapp.exceptions.SensorAlreadyExistsException;
import com.spring.myapp.models.Sensor;
import com.spring.myapp.repositories.SensorRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SensorServiceTest {

    @Mock
    private SensorRepository sensorRepository;

    @InjectMocks
    private SensorService sensorService;

    @Test
    void registerSavesNewSensor() {
        Sensor sensor = sensor("outside");
        when(sensorRepository.existsByName("outside")).thenReturn(false);

        sensorService.register(sensor);

        verify(sensorRepository).saveAndFlush(sensor);
    }

    @Test
    void registerRejectsDuplicateSensor() {
        Sensor sensor = sensor("outside");
        when(sensorRepository.existsByName("outside")).thenReturn(true);

        assertThrows(SensorAlreadyExistsException.class, () -> sensorService.register(sensor));

        verify(sensorRepository, never()).saveAndFlush(sensor);
    }

    @Test
    void registerConvertsUniqueConstraintViolationToDomainException() {
        Sensor sensor = sensor("outside");
        when(sensorRepository.existsByName("outside")).thenReturn(false);
        when(sensorRepository.saveAndFlush(sensor)).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThrows(SensorAlreadyExistsException.class, () -> sensorService.register(sensor));
    }

    private Sensor sensor(String name) {
        Sensor sensor = new Sensor();
        sensor.setName(name);
        return sensor;
    }
}
