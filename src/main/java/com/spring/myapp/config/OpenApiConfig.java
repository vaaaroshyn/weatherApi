package com.spring.myapp.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI weatherSensorApiOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Weather Sensor API")
                        .description("REST API for collecting weather measurements from registered sensors")
                        .version("v1"));
    }
}
