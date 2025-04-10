package pl.pwr.Neuralingo.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        Server localhost = new Server()
                .url("http://localhost:8080")
                .description("Local test server");

        return new OpenAPI()
                .servers(List.of(localhost))
                .info(new Info()
                        .title("Neuralingo API")
                        .version("1.0")
                        .description("Test API without login"));
    }
}
