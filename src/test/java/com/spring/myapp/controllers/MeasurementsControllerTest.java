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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.containsString;
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
    void getMeasurementsReturnsDtoResponse() throws Exception {
        Measurement measurement = measurement(false);
        MeasurementDTO measurementDTO = measurementDto(false);
        when(measurementService.findAll()).thenReturn(List.of(measurement));
        when(modelMapper.map(measurement, MeasurementDTO.class)).thenReturn(measurementDTO);

        mockMvc.perform(get("/api/v1/measurements"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.measurements[0].value").value(12.5))
                .andExpect(jsonPath("$.measurements[0].raining").value(false))
                .andExpect(jsonPath("$.measurements[0].sensor.name").value("outside"));
    }

    @Test
    void getRainyDaysCountReturnsCount() throws Exception {
        when(measurementService.findAll()).thenReturn(List.of(measurement(true), measurement(false)));

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
        Sensor sensor = new Sensor();
        sensor.setName("outside");

        Measurement measurement = new Measurement();
        measurement.setValue(12.5);
        measurement.setRaining(raining);
        measurement.setSensor(sensor);
        return measurement;
    }
}
