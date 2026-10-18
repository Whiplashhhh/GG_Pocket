package fr.gravelinesgrimp.pocket.platform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration(proxyBeanMethods = false)
class OpenApiConfig {

    /**
     * Fixed metadata so the generated contract only changes when the API changes (not with the app
     * version or the host it runs on).
     */
    @Bean
    OpenAPI ggPocketOpenApi() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("GG Pocket API")
                                .description("API of GG Pocket, the Gravelines Grimp club app.")
                                .version("v1"))
                .servers(List.of(new Server().url("/")));
    }
}
