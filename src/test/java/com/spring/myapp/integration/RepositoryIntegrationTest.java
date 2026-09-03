package com.spring.myapp.integration;

import com.spring.myapp.models.Measurement;
import com.spring.myapp.models.Sensor;
import com.spring.myapp.repositories.MeasurementRepository;
import com.spring.myapp.repositories.SensorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
class RepositoryIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private MeasurementRepository measurementRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        measurementRepository.deleteAll();
        sensorRepository.deleteAll();
    }

    @Test
    void flywayCreatesSchemaOnceForIntegrationTests() {
        Integer appliedMigrations = jdbcTemplate.queryForObject(
                "select count(*) from flyway_schema_history where success = true",
                Integer.class
        );

        assertEquals(1, appliedMigrations);
    }

    @Test
    void repositoriesPersistSensorAndMeasurementRelationship() {
        Sensor sensor = sensorRepository.save(sensor("outside"));
        Measurement measurement = measurementRepository.save(measurement(sensor, true));

        Measurement stored = measurementRepository.findById(measurement.getId()).orElseThrow();

        assertNotNull(stored.getMeasurementDateTime());
        assertEquals(sensor.getId(), stored.getSensor().getId());
        assertEquals("outside", stored.getSensor().getName());
    }

    @Test
    void repositoryCountsRainyMeasurementsInDatabase() {
        Sensor sensor = sensorRepository.save(sensor("outside"));
        measurementRepository.save(measurement(sensor, true));
        measurementRepository.save(measurement(sensor, false));
        measurementRepository.save(measurement(sensor, true));

        assertEquals(2, measurementRepository.countByRainingTrue());
    }

    @Test
    void pageableQueryFetchesSensorData() {
        Sensor sensor = sensorRepository.save(sensor("outside"));
        measurementRepository.save(measurement(sensor, false));

        Measurement stored = measurementRepository.findAll(PageRequest.of(0, 1)).getContent().get(0);

        assertEquals("outside", stored.getSensor().getName());
    }

    private Sensor sensor(String name) {
        Sensor sensor = new Sensor();
        sensor.setName(name);
        return sensor;
    }

    private Measurement measurement(Sensor sensor, boolean raining) {
        Measurement measurement = new Measurement();
        measurement.setValue(12.5);
        measurement.setRaining(raining);
        measurement.setMeasurementDateTime(LocalDateTime.now());
        measurement.setSensor(sensor);
        return measurement;
    }
}
