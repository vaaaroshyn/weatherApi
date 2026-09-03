package com.spring.myapp;

import com.spring.myapp.repositories.MeasurementRepository;
import com.spring.myapp.repositories.SensorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(properties = "spring.autoconfigure.exclude="
        + "org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration,"
        + "org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration")
class MyAppApplicationTests {

    @MockBean
    private SensorRepository sensorRepository;

    @MockBean
    private MeasurementRepository measurementRepository;

    @Test
    void contextLoads() {
    }

}
