package fr.gravelinesgrimp.pocket;

import org.springframework.boot.SpringApplication;

/**
 * Runs the application with a throw-away PostgreSQL in Docker, without docker compose: {@code
 * ./mvnw spring-boot:test-run}.
 */
public class TestGgPocketApplication {

    public static void main(String[] args) {
        SpringApplication.from(GgPocketApplication::main)
                .with(TestcontainersConfiguration.class)
                .run(args);
    }
}
