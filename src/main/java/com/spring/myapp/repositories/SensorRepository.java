package com.spring.myapp.repositories;

import com.spring.myapp.models.Sensor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SensorRepository extends JpaRepository<Sensor, Integer> {
    Optional<Sensor> findByName(String name);

    boolean existsByName(String name);
}
