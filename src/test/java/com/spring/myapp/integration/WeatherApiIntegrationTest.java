package com.spring.myapp.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.spring.myapp.repositories.MeasurementRepository;
import com.spring.myapp.repositories.SensorRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class WeatherApiIntegrationTest extends PostgresIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MeasurementRepository measurementRepository;

    @Autowired
    private SensorRepository sensorRepository;

    @BeforeEach
    void setUp() {
        measurementRepository.deleteAll();
        sensorRepository.deleteAll();
    }

    @Test
    void registerSensorPersistsDataAndReturnsCreated() throws Exception {
        mockMvc.perform(post("/api/v1/sensors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"outside"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/sensors"))
                .andExpect(content().string(""));

        assertEquals(1, sensorRepository.count());
        assertEquals("outside", sensorRepository.findByName("outside").orElseThrow().getName());
    }

    @Test
    void duplicateSensorNameReturnsConflictApiError() throws Exception {
        registerSensor("outside");

        mockMvc.perform(post("/api/v1/sensors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"outside"}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("name - There's already a sensor by that name!;"))
                .andExpect(jsonPath("$.path").value("/api/v1/sensors"));
    }

    @Test
    void addMeasurementPersistsTimestampAndSensorRelationship() throws Exception {
        registerSensor("outside");

        mockMvc.perform(post("/api/v1/measurements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"value":12.5,"raining":true,"sensor":{"name":"outside"}}
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/measurements"))
                .andExpect(content().string(""));

        assertEquals(1, measurementRepository.count());
        var measurement = measurementRepository.findAll().get(0);
        assertNotNull(measurement.getMeasurementDateTime());
        assertEquals("outside", measurement.getSensor().getName());
    }

    @Test
    void unknownSensorReturnsNotFoundApiError() throws Exception {
        mockMvc.perform(post("/api/v1/measurements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"value":12.5,"raining":false,"sensor":{"name":"missing"}}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("sensor - There is no registered sensor with that name!;"))
                .andExpect(jsonPath("$.path").value("/api/v1/measurements"));
    }

    @Test
    void getMeasurementsReturnsPageWithMeasurementResponseDtos() throws Exception {
        registerSensor("outside");
        addMeasurement(10.0, false, "outside");
        addMeasurement(20.0, true, "outside");

        mockMvc.perform(get("/api/v1/measurements")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "value,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").isNumber())
                .andExpect(jsonPath("$.content[0].value").value(10.0))
                .andExpect(jsonPath("$.content[0].raining").value(false))
                .andExpect(jsonPath("$.content[0].measuredAt").exists())
                .andExpect(jsonPath("$.content[0].sensor.id").isNumber())
                .andExpect(jsonPath("$.content[0].sensor.name").value("outside"))
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.totalPages").value(2));
    }

    @Test
    void countRainyMeasurementsUsesPersistedData() throws Exception {
        registerSensor("outside");
        addMeasurement(10.0, false, "outside");
        addMeasurement(20.0, true, "outside");
        addMeasurement(30.0, true, "outside");

        mockMvc.perform(get("/api/v1/measurements/rainy-days/count"))
                .andExpect(status().isOk())
                .andExpect(content().string("2"));
    }

    @Test
    void validationErrorContainsStableApiErrorStructure() throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/sensors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":""}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message", containsString("name - The name should not be blank!")))
                .andExpect(jsonPath("$.path").value("/api/v1/sensors"))
                .andReturn();

        JsonNode error = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals(5, error.size());
    }

    private void registerSensor(String name) throws Exception {
        mockMvc.perform(post("/api/v1/sensors")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"%s"}
                                """.formatted(name)))
                .andExpect(status().isCreated());
    }

    private void addMeasurement(double value, boolean raining, String sensorName) throws Exception {
        mockMvc.perform(post("/api/v1/measurements")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"value":%s,"raining":%s,"sensor":{"name":"%s"}}
                                """.formatted(value, raining, sensorName)))
                .andExpect(status().isCreated());
    }
}
