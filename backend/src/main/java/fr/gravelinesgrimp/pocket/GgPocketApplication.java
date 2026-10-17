package fr.gravelinesgrimp.pocket;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;

// No in-memory user with a generated password: there is no default password, anywhere.
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@ConfigurationPropertiesScan
public class GgPocketApplication {

    public static void main(String[] args) {
        SpringApplication.run(GgPocketApplication.class, args);
    }
}
