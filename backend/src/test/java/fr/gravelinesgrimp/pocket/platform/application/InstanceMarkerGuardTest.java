package fr.gravelinesgrimp.pocket.platform.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import fr.gravelinesgrimp.pocket.platform.config.GgProperties;
import fr.gravelinesgrimp.pocket.platform.domain.DeploymentEnvironment;
import fr.gravelinesgrimp.pocket.platform.domain.EnvironmentMismatchException;
import fr.gravelinesgrimp.pocket.platform.domain.InstanceMarker;
import fr.gravelinesgrimp.pocket.platform.infra.InstanceMarkerRepository;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mock.env.MockEnvironment;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;

class InstanceMarkerGuardTest {

    private final InstanceMarkerRepository repository = mock(InstanceMarkerRepository.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-10-17T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void marksAFreshDatabaseWithTheConfiguredEnvironment() {
        when(repository.findById(InstanceMarker.SINGLETON_ID)).thenReturn(Optional.empty());

        guardConfiguredFor(DeploymentEnvironment.DEV).verify();

        var saved = ArgumentCaptor.forClass(InstanceMarker.class);
        verify(repository).save(saved.capture());
        assertThat(saved.getValue().environment()).isEqualTo(DeploymentEnvironment.DEV);
        assertThat(saved.getValue().createdAt()).isEqualTo(clock.instant());
    }

    @Test
    void acceptsADatabaseOfTheSameEnvironment() {
        when(repository.findById(InstanceMarker.SINGLETON_ID))
                .thenReturn(Optional.of(marker(DeploymentEnvironment.PROD)));

        guardConfiguredFor(DeploymentEnvironment.PROD).verify();

        verify(repository, never()).save(any());
    }

    @Test
    void refusesADatabaseOfAnotherEnvironment() {
        when(repository.findById(InstanceMarker.SINGLETON_ID))
                .thenReturn(Optional.of(marker(DeploymentEnvironment.PROD)));

        assertThatThrownBy(() -> guardConfiguredFor(DeploymentEnvironment.DEV).verify())
                .isInstanceOf(EnvironmentMismatchException.class)
                .hasMessageContaining("PROD")
                .hasMessageContaining("DEV");
        verify(repository, never()).save(any());
    }

    @Test
    void refusesTheProdProfileWithoutTheProdEnvironment() {
        var prodProfile = new MockEnvironment();
        prodProfile.setActiveProfiles("prod");

        assertThatThrownBy(
                        () -> guardConfiguredFor(DeploymentEnvironment.LOCAL, prodProfile).verify())
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("GG_ENVIRONMENT");
        verify(repository, never()).save(any());
    }

    @Test
    void acceptsTheProdProfileWithTheProdEnvironment() {
        var prodProfile = new MockEnvironment();
        prodProfile.setActiveProfiles("prod");
        when(repository.findById(InstanceMarker.SINGLETON_ID))
                .thenReturn(Optional.of(marker(DeploymentEnvironment.PROD)));

        guardConfiguredFor(DeploymentEnvironment.PROD, prodProfile).verify();

        verify(repository, never()).save(any());
    }

    private InstanceMarkerGuard guardConfiguredFor(DeploymentEnvironment environment) {
        return guardConfiguredFor(environment, new MockEnvironment());
    }

    private InstanceMarkerGuard guardConfiguredFor(
            DeploymentEnvironment environment, MockEnvironment springEnvironment) {
        var properties = new GgProperties(environment, new GgProperties.Git("unknown", "unknown"));
        return new InstanceMarkerGuard(repository, properties, clock, springEnvironment);
    }

    private InstanceMarker marker(DeploymentEnvironment environment) {
        return new InstanceMarker(environment, Instant.parse("2026-01-01T00:00:00Z"));
    }
}
