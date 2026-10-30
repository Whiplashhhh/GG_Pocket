package fr.gravelinesgrimp.pocket.platform.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/** Turns on the scheduled jobs (the monthly closure of the rankings). */
@Configuration(proxyBeanMethods = false)
@EnableScheduling
class SchedulingConfig {}
