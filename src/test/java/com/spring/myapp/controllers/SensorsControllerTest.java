package com.spring.myapp.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.myapp.dto.SensorDTO;
import com.spring.myapp.exceptions.SensorAlreadyExistsException;
import com.spring.myapp.models.Sensor;
import com.spring.myapp.services.SensorService;
import org.junit.jupiter.api.Test;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SensorsController.class)
class SensorsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SensorService sensorService;

    @MockBean
    private ModelMapper modelMapper;

    @Test
    void registerSensorReturnsCreatedWithoutBody() throws Exception {
        Sensor sensor = sensor("outside");
        when(modelMapper.map(any(SensorDTO.class), eq(Sensor.class))).thenReturn(sensor);

        mockMvc.perform(post("/api/v1/sensors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sensorDto("outside"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/sensors"))
                .andExpect(content().string(""));

        verify(sensorService).register(sensor);
    }

    @Test
    void duplicateSensorReturnsConflictApiError() throws Exception {
        Sensor sensor = sensor("outside");
        when(modelMapper.map(any(SensorDTO.class), eq(Sensor.class))).thenReturn(sensor);
        doThrow(new SensorAlreadyExistsException()).when(sensorService).register(sensor);

        mockMvc.perform(post("/api/v1/sensors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sensorDto("outside"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("name - There's already a sensor by that name!;"))
                .andExpect(jsonPath("$.path").value("/api/v1/sensors"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void invalidSensorRequestReturnsBadRequestApiError() throws Exception {
        mockMvc.perform(post("/api/v1/sensors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sensorDto("x"))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString(
                        "name - The name of the sensor must be between 3 and 30 characters!")))
                .andExpect(jsonPath("$.path").value("/api/v1/sensors"));
    }

    @Test
    void blankSensorNameReturnsBadRequestApiError() throws Exception {
        mockMvc.perform(post("/api/v1/sensors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sensorDto(""))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString("name - The name should not be blank!")));
    }

    @Test
    void tooLongSensorNameReturnsBadRequestApiError() throws Exception {
        mockMvc.perform(post("/api/v1/sensors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sensorDto("x".repeat(31)))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message", containsString(
                        "name - The name of the sensor must be between 3 and 30 characters!")));
    }

    private SensorDTO sensorDto(String name) {
        SensorDTO sensorDTO = new SensorDTO();
        sensorDTO.setName(name);
        return sensorDTO;
    }

    private Sensor sensor(String name) {
        Sensor sensor = new Sensor();
        sensor.setName(name);
        return sensor;
    }
}
