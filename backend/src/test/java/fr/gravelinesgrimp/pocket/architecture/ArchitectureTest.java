package fr.gravelinesgrimp.pocket.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.Architectures.layeredArchitecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaType;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;

import jakarta.persistence.Entity;

import org.springframework.data.repository.Repository;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Stream;

/**
 * Guards the layering described in docs/ARCHITECTURE.md. Each domain package ({@code topo}, {@code
 * session}…) is split into {@code api} → {@code application} → {@code domain}, with {@code infra}
 * for persistence and {@code config} for wiring.
 */
@AnalyzeClasses(
        packages = "fr.gravelinesgrimp.pocket",
        importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final String ROOT = "fr.gravelinesgrimp.pocket";

    @ArchTest
    static final ArchRule layersAreRespected =
            layeredArchitecture()
                    .consideringOnlyDependenciesInLayers()
                    .withOptionalLayers(true)
                    .layer("Api")
                    .definedBy(ROOT + ".*.api..")
                    .layer("Application")
                    .definedBy(ROOT + ".*.application..")
                    .layer("Domain")
                    .definedBy(ROOT + ".*.domain..")
                    .layer("Infra")
                    .definedBy(ROOT + ".*.infra..")
                    .layer("Config")
                    .definedBy(ROOT + ".*.config..")
                    .whereLayer("Api")
                    .mayOnlyBeAccessedByLayers("Config")
                    .whereLayer("Application")
                    .mayOnlyBeAccessedByLayers("Api", "Config")
                    .whereLayer("Infra")
                    .mayOnlyBeAccessedByLayers("Application", "Config")
                    .whereLayer("Domain")
                    .mayOnlyBeAccessedByLayers("Api", "Application", "Infra", "Config");

    @ArchTest
    static final ArchRule domainStaysFreeOfWebAndPersistencePlumbing =
            noClasses()
                    .that()
                    .resideInAPackage(ROOT + "..domain..")
                    .should()
                    .dependOnClassesThat()
                    .resideInAnyPackage(
                            "org.springframework.web..",
                            "org.springframework.http..",
                            "jakarta.servlet..",
                            ROOT + "..api..",
                            ROOT + "..application..",
                            ROOT + "..infra..");

    @ArchTest
    static final ArchRule controllersLiveInApi =
            classes()
                    .that()
                    .areAnnotatedWith(RestController.class)
                    .should()
                    .resideInAPackage("..api..");

    @ArchTest
    static final ArchRule repositoriesLiveInInfra =
            classes()
                    .that()
                    .areAssignableTo(Repository.class)
                    .should()
                    .resideInAPackage("..infra..");

    @ArchTest
    static final ArchRule apiMethodsNeverExposeEntities =
            methods()
                    .that()
                    .areDeclaredInClassesThat()
                    .resideInAPackage("..api..")
                    .should(notExposeEntitiesInTheirSignature());

    @ArchTest
    static final ArchRule apiDtosNeverHoldEntities =
            fields().that()
                    .areDeclaredInClassesThat()
                    .resideInAPackage("..api..")
                    .should(notHaveAnEntityType())
                    .allowEmptyShould(true);

    private static ArchCondition<JavaMethod> notExposeEntitiesInTheirSignature() {
        return new ArchCondition<>("not take or return JPA entities") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                Stream.concat(
                                Stream.of(method.getReturnType()),
                                method.getParameterTypes().stream())
                        .flatMap(ArchitectureTest::involvedEntities)
                        .forEach(
                                entity ->
                                        events.add(
                                                SimpleConditionEvent.violated(
                                                        method,
                                                        method.getFullName()
                                                                + " exposes the entity "
                                                                + entity.getName())));
            }
        };
    }

    private static ArchCondition<JavaField> notHaveAnEntityType() {
        return new ArchCondition<>("not hold JPA entities") {
            @Override
            public void check(JavaField field, ConditionEvents events) {
                involvedEntities(field.getType())
                        .forEach(
                                entity ->
                                        events.add(
                                                SimpleConditionEvent.violated(
                                                        field,
                                                        field.getFullName()
                                                                + " holds the entity "
                                                                + entity.getName())));
            }
        };
    }

    private static Stream<JavaClass> involvedEntities(JavaType type) {
        return type.getAllInvolvedRawTypes().stream()
                .filter(rawType -> rawType.isAnnotatedWith(Entity.class));
    }
}
