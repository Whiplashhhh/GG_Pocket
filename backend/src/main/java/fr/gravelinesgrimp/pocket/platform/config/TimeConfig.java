package fr.gravelinesgrimp.pocket.platform.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration(proxyBeanMethods = false)
class TimeConfig {

    /** Injected wherever "now" matters, so tests can pin the time. */
    @Bean
    Clock clock() {
        return Clock.systemUTC();
    }
}
