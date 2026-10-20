package fr.gravelinesgrimp.pocket.platform.config;

import fr.gravelinesgrimp.pocket.platform.domain.DatabaseSettingsCheck;

import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

@Configuration(proxyBeanMethods = false)
class DeploymentGuardConfig {

    /**
     * Runs before any regular bean exists, so before the datasource and Flyway connect: a deployed
     * instance never touches a database with the local defaults (DECISIONS D-064).
     */
    @Bean
    static BeanFactoryPostProcessor databaseSettingsGuard(Environment environment) {
        return beanFactory ->
                DatabaseSettingsCheck.verify(
                        environment.getProperty("gg.environment", "local"),
                        environment::getProperty);
    }
}
