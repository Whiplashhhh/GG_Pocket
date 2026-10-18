package fr.gravelinesgrimp.pocket.platform.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import fr.gravelinesgrimp.pocket.IntegrationTest;
import fr.gravelinesgrimp.pocket.platform.domain.EnvironmentMismatchException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

@IntegrationTest
class InstanceMarkerGuardIT {

    @Autowired private InstanceMarkerGuard guard;
    @Autowired private JdbcTemplate jdbc;

    @Test
    void startupMarkedTheFreshDatabaseAsLocal() {
        assertThat(jdbc.queryForObject("select environment from instance_marker", String.class))
                .isEqualTo("LOCAL");
    }

    @Test
    void refusesADatabaseMarkedForAnotherEnvironment() {
        jdbc.update("update instance_marker set environment = 'PROD'");
        try {
            assertThatThrownBy(guard::verify).isInstanceOf(EnvironmentMismatchException.class);
        } finally {
            jdbc.update("update instance_marker set environment = 'LOCAL'");
        }
    }
}
