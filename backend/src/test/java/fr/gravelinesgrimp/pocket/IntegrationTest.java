package fr.gravelinesgrimp.pocket;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Full application against a real PostgreSQL (Testcontainers), driven through MockMvc with the real
 * security filter chain. Every integration test uses this exact annotation so they all share one
 * cached Spring context and one database container.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@SpringBootTest(properties = "gg.media.root=target/test-media")
@AutoConfigureMockMvc
@Import({
    TestcontainersConfiguration.class,
    MemberFixtures.class,
    RouteFixtures.class,
    SessionFixtures.class
})
public @interface IntegrationTest {}
