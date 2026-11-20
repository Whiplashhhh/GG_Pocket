package fr.gravelinesgrimp.pocket.notification.config;

import fr.gravelinesgrimp.pocket.notification.domain.PushGateway;
import fr.gravelinesgrimp.pocket.notification.domain.WebPush;
import fr.gravelinesgrimp.pocket.notification.infra.HttpPushGateway;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

/** Wiring of push (D-130): keys read once at start-up, sends off the request's thread. */
@Configuration
class PushConfig {

    /** Wrong keys stop the start-up: better than a server that silently sends nothing. */
    @Bean
    PushKeys pushKeys(PushProperties properties) {
        if (!properties.enabled()) {
            return new PushKeys(null, "");
        }
        var subject = properties.subject().strip();
        if (!subject.startsWith("mailto:") && !subject.startsWith("https://")) {
            throw new IllegalStateException(
                    "GG_PUSH_SUBJECT must be a mailto: or https: URL when push is on");
        }
        return new PushKeys(
                WebPush.VapidKeys.of(properties.publicKey(), properties.privateKey()), subject);
    }

    @Bean
    PushGateway pushGateway(PushProperties properties) {
        return new HttpPushGateway(properties.relay());
    }

    /** One virtual thread per message: a slow push service never holds a request. */
    @Bean
    PushRunner pushRunner() {
        var executor = Executors.newVirtualThreadPerTaskExecutor();
        return executor::execute;
    }
}
