package com.spring.myapp.repositories;

import com.spring.myapp.models.Measurement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MeasurementRepository extends JpaRepository<Measurement, Integer> {
    @EntityGraph(attributePaths = "sensor")
    Page<Measurement> findAll(Pageable pageable);

    long countByRainingTrue();
}
