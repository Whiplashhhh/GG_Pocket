package fr.gravelinesgrimp.pocket.auth.config;

import fr.gravelinesgrimp.pocket.auth.domain.PasswordPolicy;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.stream.Collectors;

@Configuration(proxyBeanMethods = false)
class PasswordPolicyConfig {

    static final String COMMON_PASSWORDS = "auth/common-passwords.txt";

    @Bean
    PasswordPolicy passwordPolicy() {
        try (var input = new ClassPathResource(COMMON_PASSWORDS).getInputStream()) {
            var common =
                    new String(input.readAllBytes(), StandardCharsets.UTF_8)
                            .lines()
                            .map(String::strip)
                            .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                            .collect(Collectors.toSet());
            return new PasswordPolicy(common);
        } catch (IOException e) {
            throw new UncheckedIOException("Missing " + COMMON_PASSWORDS, e);
        }
    }
}
