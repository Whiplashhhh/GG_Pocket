package fr.gravelinesgrimp.pocket.platform.application;

import fr.gravelinesgrimp.pocket.platform.config.GgProperties;
import fr.gravelinesgrimp.pocket.platform.domain.EnvironmentMismatchException;
import fr.gravelinesgrimp.pocket.platform.domain.InstanceMarker;
import fr.gravelinesgrimp.pocket.platform.infra.InstanceMarkerRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.SmartInitializingSingleton;
import org.springframework.stereotype.Component;

import java.time.Clock;

/**
 * Marks a fresh database with the configured environment, then refuses to start whenever the
 * application is pointed at a database of another environment (for instance a dev instance plugged
 * into the production database).
 *
 * <p>Runs once all beans exist, before the web server accepts any request.
 */
@Component
public class InstanceMarkerGuard implements SmartInitializingSingleton {

    private static final Logger log = LoggerFactory.getLogger(InstanceMarkerGuard.class);

    private final InstanceMarkerRepository repository;
    private final GgProperties properties;
    private final Clock clock;

    public InstanceMarkerGuard(
            InstanceMarkerRepository repository, GgProperties properties, Clock clock) {
        this.repository = repository;
        this.properties = properties;
        this.clock = clock;
    }

    @Override
    public void afterSingletonsInstantiated() {
        verify();
    }

    /**
     * @throws EnvironmentMismatchException if the database belongs to another environment
     */
    public void verify() {
        var configured = properties.environment();
        var existing = repository.findById(InstanceMarker.SINGLETON_ID);
        if (existing.isEmpty()) {
            repository.save(new InstanceMarker(configured, clock.instant()));
            log.info("Database marked as belonging to the {} environment", configured);
            return;
        }
        var marker = existing.get().environment();
        if (marker != configured) {
            throw new EnvironmentMismatchException(configured, marker);
        }
    }
}
