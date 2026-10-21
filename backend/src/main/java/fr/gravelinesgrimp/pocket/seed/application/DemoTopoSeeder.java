package fr.gravelinesgrimp.pocket.seed.application;

import fr.gravelinesgrimp.pocket.club.domain.Cord;
import fr.gravelinesgrimp.pocket.club.domain.Grade;
import fr.gravelinesgrimp.pocket.club.domain.HoldColor;
import fr.gravelinesgrimp.pocket.club.infra.CordRepository;
import fr.gravelinesgrimp.pocket.club.infra.GradeRepository;
import fr.gravelinesgrimp.pocket.club.infra.HoldColorRepository;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.platform.domain.TimestampedEntity;
import fr.gravelinesgrimp.pocket.topo.domain.CheckSource;
import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;
import fr.gravelinesgrimp.pocket.topo.domain.Favorite;
import fr.gravelinesgrimp.pocket.topo.domain.LastChanceAck;
import fr.gravelinesgrimp.pocket.topo.domain.Route;
import fr.gravelinesgrimp.pocket.topo.domain.RouteCheck;
import fr.gravelinesgrimp.pocket.topo.domain.RouteComment;
import fr.gravelinesgrimp.pocket.topo.domain.RouteDraft;
import fr.gravelinesgrimp.pocket.topo.domain.RouteStyle;
import fr.gravelinesgrimp.pocket.topo.infra.FavoriteRepository;
import fr.gravelinesgrimp.pocket.topo.infra.LastChanceAckRepository;
import fr.gravelinesgrimp.pocket.topo.infra.RouteCheckRepository;
import fr.gravelinesgrimp.pocket.topo.infra.RouteCommentRepository;
import fr.gravelinesgrimp.pocket.topo.infra.RouteRepository;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The demo topo (spec §17): 60 routes over the 29 cords, every grade from 4a to 7c (mostly 5 and
 * 6), 5 dismounted, 3 in last chance, with comments, hearts (the {@code grimpeur} account has a
 * full heart, plus one on a dismounted route) and a few "je sais la faire". Fixed lists, no
 * randomness: every run gives the same topo. No photo yet (phase 8).
 *
 * <p>Written straight through the entities: the seeder runs at startup, without a signed-in member
 * for the secured services. The entities still apply their own rules (statuses, colours).
 */
@Component
@Profile("dev")
public class DemoTopoSeeder {

    /** Fictional names in the spirit of the club, one per route. */
    static final List<String> NAMES =
            List.of(
                    "Bac à sable",
                    "Le petit pan du dimanche",
                    "Magnésie partout",
                    "Chausson qui bâille",
                    "Dalle de la honte",
                    "Le réveil musculaire",
                    "Avant-bras en béton",
                    "Pas de pied, pas de chocolat",
                    "La moulinette infernale",
                    "Mollets de coq",
                    "Le crux du goûter",
                    "Corde tendue, cœur serré",
                    "Bras de T-Rex",
                    "Le gaston du lundi",
                    "Réglette mon amour",
                    "La pince à linge",
                    "Plat du jour",
                    "Talon-pointe au bar",
                    "Le dièdre à Mémé",
                    "Croûte de bac",
                    "Assure-moi si tu peux",
                    "La vache qui grimpe",
                    "Mou du genou",
                    "Le toit, c'est moi",
                    "Dévers et contre tout",
                    "Prise de tête",
                    "Le jeté du vendredi",
                    "Pof, pof et re-pof",
                    "Sous le toit, personne ne t'entend crier",
                    "Cheminée à ramoner",
                    "Pédale dans la choucroute",
                    "La dalle qui glisse",
                    "Il est où le bac ?",
                    "Coinceur de doigts",
                    "Mouli party",
                    "Fesses au mur",
                    "Le yoyo de l'ouvreur",
                    "Bidoigt bidon",
                    "Épaule de marbre",
                    "Le grand saut",
                    "Gratton gratin",
                    "Crimp and punishment",
                    "Pieds de plomb",
                    "Dégaine-toi de là",
                    "Lolotte en folie",
                    "Le vol plané",
                    "Coup de bec",
                    "Inversée comme ma vie",
                    "Le bout du rouleau",
                    "La dernière avant la bière",
                    "Chaussons neufs, ego usé",
                    "Mi-figue, mi-réglette",
                    "Morpho dépendante",
                    "Ça passe en tête ?",
                    "Le pas du chat",
                    "Rési à la carte",
                    "Ventouse",
                    "Le baquet magique",
                    "Tendinite express",
                    "Sec comme un coup de trique");

    /** One grade per route: every grade at least once, mostly 5 and 6. */
    static final List<String> GRADES =
            List.of(
                    "4a", "5a", "6a", "4a+", "5a+", "6a+", "4b", "5b", "6b", "4b+", "5b+", "6b+",
                    "4c", "5c", "6c", "4c+", "5c+", "6c+", "7a", "7a+", "7b", "7b+", "7c", "5a",
                    "6a", "5b", "6a+", "5c", "6b", "5a+", "6b+", "5b+", "6c", "5c+", "6a", "5c",
                    "6a+", "5b", "6b", "7a", "6c+", "5a", "6a", "4c", "5c+", "6b+", "6c", "7a+",
                    "5b+", "6a", "5c", "6b", "6a+", "5a+", "6c", "7b", "5c", "6b+", "6a", "5b");

    private static final List<String> COLORS =
            List.of(
                    "Jaune",
                    "Orange",
                    "Rouge",
                    "Rose",
                    "Violet",
                    "Bleu",
                    "Bleu clair",
                    "Vert",
                    "Vert clair",
                    "Noir",
                    "Blanc",
                    "Gris",
                    "Marron",
                    "Multicolore");

    private static final List<List<RouteStyle>> STYLES =
            List.of(
                    List.of(RouteStyle.SLAB, RouteStyle.TECHNICAL),
                    List.of(RouteStyle.OVERHANG, RouteStyle.PHYSICAL, RouteStyle.JUGS),
                    List.of(RouteStyle.CONTINUITY),
                    List.of(RouteStyle.CRIMPS, RouteStyle.TECHNICAL),
                    List.of(RouteStyle.ROOF, RouteStyle.PHYSICAL),
                    List.of(RouteStyle.DIHEDRAL, RouteStyle.COMPLEX),
                    List.of(RouteStyle.ENDURANCE, RouteStyle.CONTINUITY, RouteStyle.JUGS),
                    List.of(RouteStyle.PINCHES, RouteStyle.SLOPERS),
                    List.of(RouteStyle.REACHY, RouteStyle.PHYSICAL),
                    List.of(),
                    List.of(RouteStyle.SLOPERS, RouteStyle.TECHNICAL, RouteStyle.SLAB),
                    List.of(RouteStyle.CRIMPS, RouteStyle.OVERHANG, RouteStyle.ENDURANCE));

    private static final List<String> ADVICE =
            List.of(
                    "Pieds hauts, bassin collé au mur, et ça passe tout seul.",
                    "Garde des bras pour la sortie : elle est plus fourbe qu'elle n'en a l'air.",
                    "Le bac est plus loin que prévu. Engage, il t'attend.",
                    "Un talon au troisième point et tu respires enfin.",
                    "Ne mousquetonne pas au-dessus de ta tête au crux, c'est un piège à vol.",
                    "Lolotte à gauche, puis main droite dans le plat. Fais-moi confiance.",
                    "Les pieds sont minuscules, mais ils tiennent. Promis.",
                    "Repose-toi sur le gros bac avant le dévers, sinon c'est le vol plané.",
                    "Prends la pince avec le pouce, pas avec l'ego.",
                    "Le jeté se fait les hanches au mur, pas les bras tendus.",
                    "Croise les mains sur la réglette : ça surprend, mais ça passe.",
                    "Monte les pieds avant de paniquer. Puis panique si besoin.",
                    "La sortie est à droite, pas tout droit. Tout le monde tombe là.",
                    "C'est de la conti : grimpe vite, réfléchis après.",
                    "Magnésie avant le toit, tu me remercieras.");

    /** Setters without an account (spec §7): nobody real. */
    private static final List<String> SETTER_NOTES =
            List.of("Le stagiaire de l'été", "Un ouvreur de passage", "L'équipe du club voisin");

    private static final List<Integer> DISMOUNTED = List.of(3, 14, 27, 40, 51);
    private static final List<Integer> LAST_CHANCE = List.of(7, 22, 45);

    record DemoComment(int route, String author, String body) {}

    private static final List<DemoComment> COMMENTS =
            List.of(
                    new DemoComment(0, "grimpeur", "Parfaite pour s'échauffer, merci !"),
                    new DemoComment(0, "ouvreur", "C'était le but. Bienvenue au club."),
                    new DemoComment(10, "direction", "Le crux m'a mangé tout cru. Je reviendrai."),
                    new DemoComment(10, "grimpeur", "Enchaînée au troisième essai, trop contente."),
                    new DemoComment(12, "admin", "Bras de T-Rex confirmés après ça."),
                    new DemoComment(
                            18, "ouvreur", "Le dièdre se grimpe en opposition, pas en force."),
                    new DemoComment(22, "grimpeur", "Noooon, pas elle, pas maintenant !"),
                    new DemoComment(22, "direction", "Elle a bien vécu. Paix à ses prises."),
                    new DemoComment(3, "grimpeur", "Ma préférée, démontée trop tôt."),
                    new DemoComment(30, "admin", "Quelqu'un a retrouvé ma choucroute ?"),
                    new DemoComment(41, "direction", "Les réglettes coupent, pensez au strap."),
                    new DemoComment(
                            55, "grimpeur", "La rési, c'est dans la tête. Et les avant-bras."));

    private final RouteRepository routes;
    private final RouteCommentRepository comments;
    private final FavoriteRepository favorites;
    private final RouteCheckRepository checks;
    private final LastChanceAckRepository acknowledgements;
    private final GradeRepository grades;
    private final CordRepository cords;
    private final HoldColorRepository holdColors;

    DemoTopoSeeder(
            RouteRepository routes,
            RouteCommentRepository comments,
            FavoriteRepository favorites,
            RouteCheckRepository checks,
            LastChanceAckRepository acknowledgements,
            GradeRepository grades,
            CordRepository cords,
            HoldColorRepository holdColors) {
        this.routes = routes;
        this.comments = comments;
        this.favorites = favorites;
        this.checks = checks;
        this.acknowledgements = acknowledgements;
        this.grades = grades;
        this.cords = cords;
        this.holdColors = holdColors;
    }

    /**
     * Seeds the topo once: does nothing if the club already has routes.
     *
     * @param accounts the demo accounts by nickname ({@code admin}, {@code direction}, {@code
     *     ouvreur}, {@code grimpeur})
     * @return the number of routes created
     */
    @Transactional(propagation = Propagation.MANDATORY)
    public int seed(Map<String, Member> accounts, Instant now, LocalDate today) {
        if (routes.count() > 0) {
            return 0;
        }
        var gradeIds = byKey(grades.findAll(), Grade::code);
        var cordIds = byKey(cords.findAll(), Cord::number);
        var colorIds = byKey(holdColors.findAll(), HoldColor::label);
        var setters =
                List.of(
                        accounts.get("ouvreur"),
                        accounts.get("ouvreur"),
                        accounts.get("direction"),
                        accounts.get("admin"));

        var created = new ArrayList<Route>();
        for (var i = 0; i < NAMES.size(); i++) {
            var colors = new ArrayList<>(List.of(colorIds.get(COLORS.get(i * 5 % COLORS.size()))));
            if (i % 7 == 2) {
                colors.add(colorIds.get(COLORS.get((i * 5 + 3) % COLORS.size())));
            }
            var setterIds = new ArrayList<>(List.of(setters.get(i % setters.size()).id()));
            if (i % 9 == 4) {
                setterIds.add(accounts.get("direction").id());
            }
            String setterNote = null;
            if (i % 13 == 5) {
                setterNote = SETTER_NOTES.get(i % SETTER_NOTES.size());
                if (i % 26 == 5) {
                    // An outside setter only, without any member.
                    setterIds.clear();
                }
            }
            var draft =
                    new RouteDraft(
                            NAMES.get(i),
                            gradeIds.get(GRADES.get(i)),
                            cordIds.get(i % 29 + 1),
                            List.copyOf(colors),
                            today.minusDays(5 + i * 3L),
                            setterIds.stream().distinct().toList(),
                            setterNote,
                            i % 2 == 0 ? ADVICE.get(i / 2 % ADVICE.size()) : null,
                            STYLES.get(i % STYLES.size()));
            draft.check(today);
            var route = new Route(draft, setters.get(i % setters.size()).id());
            var dismounted = DISMOUNTED.indexOf(i);
            if (dismounted >= 0) {
                route.dismount(now.minus(Duration.ofDays(2 + dismounted * 7L)));
            }
            if (LAST_CHANCE.contains(i)) {
                route.announceLastChance(i == 7 ? today.plusDays(10) : null, today);
            }
            created.add(route);
        }
        routes.saveAll(created);
        routes.flush();

        for (var comment : COMMENTS) {
            comments.save(
                    new RouteComment(
                            created.get(comment.route()).id(),
                            accounts.get(comment.author()).id(),
                            comment.body()));
        }
        var grimpeur = accounts.get("grimpeur");
        // A full heart (2 on the wall) plus one on a dismounted route, which frees its place.
        heart(grimpeur, created.get(0));
        heart(grimpeur, created.get(10));
        heart(grimpeur, created.get(3));
        heart(accounts.get("direction"), created.get(22));
        heart(accounts.get("direction"), created.get(30));
        heart(accounts.get("ouvreur"), created.get(10));
        heart(accounts.get("admin"), created.get(7));
        grimpeur.changeFavoriteRoute(created.get(3).id());

        check(grimpeur, created.get(0), ClimbMode.TOP_ROPE);
        check(grimpeur, created.get(0), ClimbMode.LEAD);
        check(grimpeur, created.get(1), ClimbMode.TOP_ROPE);
        check(grimpeur, created.get(10), ClimbMode.LEAD);
        check(accounts.get("direction"), created.get(22), ClimbMode.LEAD);

        // The grimpeur account has seen nothing yet: the last chance pop-up shows up.
        acknowledgements.save(new LastChanceAck(accounts.get("ouvreur").id(), created.get(7).id()));
        acknowledgements.save(
                new LastChanceAck(accounts.get("ouvreur").id(), created.get(22).id()));
        acknowledgements.save(
                new LastChanceAck(accounts.get("direction").id(), created.get(22).id()));
        return created.size();
    }

    private void heart(Member member, Route route) {
        favorites.save(new Favorite(member.id(), route.id()));
    }

    private void check(Member member, Route route, ClimbMode mode) {
        checks.save(new RouteCheck(member.id(), route.id(), mode, CheckSource.MANUAL));
    }

    private static <T extends TimestampedEntity, K> Map<K, UUID> byKey(
            List<T> items, Function<T, K> key) {
        return items.stream().collect(Collectors.toMap(key, item -> item.id()));
    }
}
