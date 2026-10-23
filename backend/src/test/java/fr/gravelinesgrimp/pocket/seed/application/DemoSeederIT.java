package fr.gravelinesgrimp.pocket.seed.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import fr.gravelinesgrimp.pocket.TestcontainersConfiguration;
import fr.gravelinesgrimp.pocket.auth.infra.RegistrationCodeRepository;
import fr.gravelinesgrimp.pocket.club.infra.GradeRepository;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.domain.Role;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.topo.domain.Route;
import fr.gravelinesgrimp.pocket.topo.domain.RouteStatus;
import fr.gravelinesgrimp.pocket.topo.infra.FavoriteRepository;
import fr.gravelinesgrimp.pocket.topo.infra.RouteCommentRepository;
import fr.gravelinesgrimp.pocket.topo.infra.RouteRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * The demo seed in the dev profile, on its own database (not {@code @IntegrationTest}: the seed
 * must start from an empty club, and its accounts must not leak into the other tests).
 */
@SpringBootTest(
        properties = {
            "spring.profiles.active=dev",
            "gg.demo-seed.allowed=true",
            "gg.media.root=target/test-media"
        })
@Import(TestcontainersConfiguration.class)
class DemoSeederIT {

    @Autowired private MemberRepository members;
    @Autowired private RegistrationCodeRepository codes;
    @Autowired private PasswordEncoder passwordEncoder;
    @Autowired private DemoSeeder seeder;
    @Autowired private RouteRepository routes;
    @Autowired private FavoriteRepository favorites;
    @Autowired private RouteCommentRepository comments;
    @Autowired private GradeRepository grades;

    @Test
    void createsOneAccountPerRoleAndADemoCode() {
        assertThat(members.findAll())
                .extracting(Member::nickname, Member::role)
                .containsExactlyInAnyOrder(
                        tuple("admin", Role.ADMIN),
                        tuple("direction", Role.BOARD),
                        tuple("ouvreur", Role.SETTER),
                        tuple("grimpeur", Role.CLIMBER));
        assertThat(members.findByNicknameIgnoreCase("grimpeur"))
                .get()
                .satisfies(
                        member ->
                                assertThat(
                                                passwordEncoder.matches(
                                                        DemoSeeder.LOCAL_PASSWORD,
                                                        member.passwordHash()))
                                        .isTrue());
        assertThat(codes.existsByCode(DemoSeeder.DEMO_CODE)).isTrue();
    }

    @Test
    void theFirstStartCodeStaysQuietAfterTheSeed() {
        assertThat(codes.findAllByGrantedRoleAndActiveTrue(Role.ADMIN)).isEmpty();
    }

    @Test
    void seedsARealisticTopo() {
        var all = routes.findAll();
        assertThat(all).hasSize(DemoTopoSeeder.NAMES.size());
        assertThat(all)
                .filteredOn(route -> route.status() == RouteStatus.DISMOUNTED)
                .hasSize(5)
                .allSatisfy(route -> assertThat(route.dismountedAt()).isNotNull());
        assertThat(all)
                .filteredOn(route -> route.status() == RouteStatus.LAST_CHANCE)
                .hasSize(3)
                .anySatisfy(route -> assertThat(route.plannedDismountOn()).isNotNull());
        assertThat(all.stream().map(Route::cordId).distinct()).hasSize(29);
        assertThat(all.stream().map(Route::gradeId).distinct()).hasSize((int) grades.count());
        assertThat(all).filteredOn(route -> route.setterNote() != null).isNotEmpty();
        assertThat(comments.count()).isPositive();
    }

    @Test
    void theGrimpeurAccountHasAFullHeartAndAHeartOnADismountedRoute() {
        var grimpeur = members.findByNicknameIgnoreCase("grimpeur").orElseThrow();
        assertThat(favorites.findRouteIdsStillOnTheWall(grimpeur.id(), RouteStatus.DISMOUNTED))
                .hasSize(2);
        assertThat(favorites.findRouteIdsByMemberId(grimpeur.id())).hasSize(3);
        assertThat(grimpeur.favoriteRouteId()).isNotNull();
    }

    @Test
    void runningItAgainChangesNothing() {
        seeder.run(null);
        assertThat(members.count()).isEqualTo(4);
        assertThat(routes.count()).isEqualTo(DemoTopoSeeder.NAMES.size());
    }
}
