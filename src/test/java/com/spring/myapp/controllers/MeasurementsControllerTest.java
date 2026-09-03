package com.spring.myapp.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.myapp.dto.MeasurementDTO;
import com.spring.myapp.dto.SensorDTO;
import com.spring.myapp.exceptions.SensorNotFoundException;
import com.spring.myapp.models.Measurement;
import com.spring.myapp.models.Sensor;
import com.spring.myapp.services.MeasurementService;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MeasurementsController.class)
class MeasurementsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MeasurementService measurementService;

    @MockBean
    private ModelMapper modelMapper;

    @Test
    void addMeasurementReturnsCreatedWithoutBody() throws Exception {
        Measurement measurement = measurement(false);
        when(modelMapper.map(any(MeasurementDTO.class), eq(Measurement.class))).thenReturn(measurement);

        mockMvc.perform(post("/api/v1/measurements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(measurementDto(false))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/measurements"))
                .andExpect(content().string(""));

        verify(measurementService).addMeasurement(measurement);
    }

    @Test
    void unknownSensorReturnsNotFoundApiError() throws Exception {
        Measurement measurement = measurement(false);
        when(modelMapper.map(any(MeasurementDTO.class), eq(Measurement.class))).thenReturn(measurement);
        doThrow(new SensorNotFoundException()).when(measurementService).addMeasurement(measurement);

        mockMvc.perform(post("/api/v1/measurements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(measurementDto(false))))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("sensor - There is no registered sensor with that name!;"))
                .andExpect(jsonPath("$.path").value("/api/v1/measurements"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void invalidMeasurementRequestReturnsBadRequestApiError() throws Exception {
        mockMvc.perform(post("/api/v1/measurements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"value":200,"raining":false,"sensor":{"name":"outside"}}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("value - must be less than or equal to 100")))
                .andExpect(jsonPath("$.path").value("/api/v1/measurements"));
    }

    @Test
    void missingTemperatureReturnsBadRequestApiError() throws Exception {
        mockMvc.perform(post("/api/v1/measurements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"raining":false,"sensor":{"name":"outside"}}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("value - must not be null")));
    }

    @Test
    void temperatureBelowAllowedRangeReturnsBadRequestApiError() throws Exception {
        mockMvc.perform(post("/api/v1/measurements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"value":-101,"raining":false,"sensor":{"name":"outside"}}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("value - must be greater than or equal to -100")));
    }

    @Test
    void missingRainingReturnsBadRequestApiError() throws Exception {
        mockMvc.perform(post("/api/v1/measurements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"value":12.5,"sensor":{"name":"outside"}}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("raining - must not be null")));
    }

    @Test
    void invalidJsonReturnsBadRequestApiError() throws Exception {
        mockMvc.perform(post("/api/v1/measurements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"value":"warm","raining":false,"sensor":{"name":"outside"}}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Invalid request body or JSON value type"))
                .andExpect(jsonPath("$.path").value("/api/v1/measurements"));
    }

    @Test
    void getMeasurementsReturnsFirstPage() throws Exception {
        Pageable pageable = PageRequest.of(0, 1);
        when(measurementService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(measurement(1, false)), pageable, 2));

        mockMvc.perform(get("/api/v1/measurements")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.content[0].value").value(12.5))
                .andExpect(jsonPath("$.content[0].raining").value(false))
                .andExpect(jsonPath("$.content[0].measuredAt").value("2026-09-03T08:00:00"))
                .andExpect(jsonPath("$.content[0].sensor.id").value(10))
                .andExpect(jsonPath("$.content[0].sensor.name").value("outside"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void getMeasurementsReturnsNextPage() throws Exception {
        Pageable pageable = PageRequest.of(1, 1);
        when(measurementService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(measurement(2, true)), pageable, 2));

        mockMvc.perform(get("/api/v1/measurements")
                        .param("page", "1")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(2))
                .andExpect(jsonPath("$.content[0].raining").value(true))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void getMeasurementsPassesSortToService() throws Exception {
        when(measurementService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 20), 0));

        mockMvc.perform(get("/api/v1/measurements")
                        .param("sort", "value,desc"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(measurementService).findAll(pageableCaptor.capture());
        Sort.Order order = pageableCaptor.getValue().getSort().getOrderFor("value");
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    void getMeasurementsCapsOversizedPageSize() throws Exception {
        when(measurementService.findAll(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 100), 0));

        mockMvc.perform(get("/api/v1/measurements")
                        .param("size", "500"))
                .andExpect(status().isOk());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(measurementService).findAll(pageableCaptor.capture());
        assertEquals(100, pageableCaptor.getValue().getPageSize());
    }

    @Test
    void getRainyDaysCountReturnsCount() throws Exception {
        when(measurementService.countRainyMeasurements()).thenReturn(1L);

        mockMvc.perform(get("/api/v1/measurements/rainy-days/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    private MeasurementDTO measurementDto(Boolean raining) {
        MeasurementDTO measurementDTO = new MeasurementDTO();
        measurementDTO.setValue(12.5);
        measurementDTO.setRaining(raining);
        measurementDTO.setSensor(sensorDto("outside"));
        return measurementDTO;
    }

    private SensorDTO sensorDto(String name) {
        SensorDTO sensorDTO = new SensorDTO();
        sensorDTO.setName(name);
        return sensorDTO;
    }

    private Measurement measurement(Boolean raining) {
        return measurement(1, raining);
    }

    private Measurement measurement(Integer id, Boolean raining) {
        Sensor sensor = new Sensor();
        sensor.setId(10);
        sensor.setName("outside");

        Measurement measurement = new Measurement();
        measurement.setId(id);
        measurement.setValue(12.5);
        measurement.setRaining(raining);
        measurement.setMeasurementDateTime(LocalDateTime.of(2026, 9, 3, 8, 0));
        measurement.setSensor(sensor);
        return measurement;
    }
}
