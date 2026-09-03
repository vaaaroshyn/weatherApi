package com.spring.myapp.services;

import com.spring.myapp.exceptions.SensorAlreadyExistsException;
import com.spring.myapp.models.Sensor;
import com.spring.myapp.repositories.SensorRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class SensorService {

    private final SensorRepository sensorRepository;

    public SensorService(SensorRepository sensorRepository) {
        this.sensorRepository = sensorRepository;
    }

    public Optional<Sensor> findByName(String name) {
        return sensorRepository.findByName(name);
    }

    @Transactional
    public void register(Sensor sensor) {
        if (sensorRepository.existsByName(sensor.getName())) {
            throw new SensorAlreadyExistsException();
        }

        try {
            sensorRepository.saveAndFlush(sensor);
        } catch (DataIntegrityViolationException e) {
            throw new SensorAlreadyExistsException(e);
        }
    }
}
