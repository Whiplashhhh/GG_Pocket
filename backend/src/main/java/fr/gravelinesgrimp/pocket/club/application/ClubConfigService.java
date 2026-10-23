package fr.gravelinesgrimp.pocket.club.application;

import fr.gravelinesgrimp.pocket.admin.application.AuditLog;
import fr.gravelinesgrimp.pocket.admin.domain.AuditAction;
import fr.gravelinesgrimp.pocket.admin.domain.AuditTargetType;
import fr.gravelinesgrimp.pocket.club.domain.ClubLayout;
import fr.gravelinesgrimp.pocket.club.domain.Cord;
import fr.gravelinesgrimp.pocket.club.domain.Grade;
import fr.gravelinesgrimp.pocket.club.domain.HoldColor;
import fr.gravelinesgrimp.pocket.club.domain.Sector;
import fr.gravelinesgrimp.pocket.club.domain.SectorOrdering;
import fr.gravelinesgrimp.pocket.club.infra.CordRepository;
import fr.gravelinesgrimp.pocket.club.infra.HoldColorRepository;
import fr.gravelinesgrimp.pocket.club.infra.SectorRepository;
import fr.gravelinesgrimp.pocket.member.domain.Member;
import fr.gravelinesgrimp.pocket.member.infra.MemberRepository;
import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * The layout of the gym (spec §7): read by every member, changed by the admin only (spec §5).
 * Nothing is ever deleted, so routes and their history keep their cord, colours and grade; every
 * change is in the audit log.
 */
@Service
public class ClubConfigService {

    /** No row has this id: "taken by another" then means "taken at all". */
    private static final UUID NO_ID = new UUID(0, 0);

    public record TopoConfigView(
            List<SectorView> sectors, List<HoldColorView> holdColors, List<GradeView> grades) {}

    /** A sector and its cords, sorted by number. */
    public record SectorView(
            UUID id, String name, int position, String description, List<CordView> cords) {}

    public record CordView(UUID id, int number, UUID sectorId, boolean active) {}

    public record HoldColorView(
            UUID id, String label, String hex, boolean multicolor, boolean active, int position) {}

    public record GradeView(UUID id, String code, int rank) {}

    public record SectorSettings(String name, String description) {}

    public record CordSettings(int number, UUID sectorId, boolean active) {}

    public record HoldColorSettings(String label, String hex, boolean multicolor, boolean active) {}

    private final ClubLayoutReader layouts;
    private final SectorRepository sectors;
    private final CordRepository cords;
    private final HoldColorRepository holdColors;
    private final MemberRepository members;
    private final AuditLog auditLog;

    public ClubConfigService(
            ClubLayoutReader layouts,
            SectorRepository sectors,
            CordRepository cords,
            HoldColorRepository holdColors,
            MemberRepository members,
            AuditLog auditLog) {
        this.layouts = layouts;
        this.sectors = sectors;
        this.cords = cords;
        this.holdColors = holdColors;
        this.members = members;
        this.auditLog = auditLog;
    }

    @PreAuthorize("hasRole('CLIMBER')")
    @Transactional(readOnly = true)
    public TopoConfigView config() {
        return toConfigView(layouts.read());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public SectorView createSector(UUID actorId, SectorSettings settings) {
        var actor = load(actorId);
        checkSectorName(settings.name(), NO_ID);
        var last = sectors.findAll().stream().mapToInt(Sector::position).max().orElse(0);
        var sector = sectors.save(new Sector(settings.name(), settings.description(), last + 1));
        auditLog.record(
                actor,
                AuditAction.CLUB_SECTOR_CREATED,
                AuditTargetType.SECTOR,
                sector.id(),
                sector.name(),
                null,
                describe(sector));
        return toSectorView(sector, List.of());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public SectorView updateSector(UUID actorId, UUID sectorId, SectorSettings settings) {
        var actor = load(actorId);
        var sector = loadSector(sectorId);
        checkSectorName(settings.name(), sectorId);
        var before = describe(sector);
        sector.rename(settings.name(), settings.description());
        auditLog.record(
                actor,
                AuditAction.CLUB_SECTOR_UPDATED,
                AuditTargetType.SECTOR,
                sector.id(),
                sector.name(),
                before,
                describe(sector));
        return toSectorView(sector, cords.findAllBySectorId(sectorId));
    }

    /** {@code sectorIds} lists every sector, in the new display order. */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public TopoConfigView reorderSectors(UUID actorId, List<UUID> sectorIds) {
        var actor = load(actorId);
        var all = sectors.findAll();
        SectorOrdering.check(sectorIds, all.stream().map(Sector::id).toList());
        var before = names(all);
        var byId = all.stream().collect(Collectors.toMap(Sector::id, sector -> sector));
        for (var i = 0; i < sectorIds.size(); i++) {
            byId.get(sectorIds.get(i)).moveTo(i + 1);
        }
        auditLog.record(
                actor,
                AuditAction.CLUB_SECTORS_REORDERED,
                AuditTargetType.SECTOR,
                null,
                "Secteurs",
                before,
                names(all));
        return toConfigView(layouts.read());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CordView createCord(UUID actorId, CordSettings settings) {
        var actor = load(actorId);
        var sector = loadSector(settings.sectorId());
        checkCordNumber(settings.number(), NO_ID);
        var cord = cords.save(new Cord(settings.number(), sector.id(), settings.active()));
        auditLog.record(
                actor,
                AuditAction.CLUB_CORD_CREATED,
                AuditTargetType.CORD,
                cord.id(),
                cordLabel(cord),
                null,
                describe(cord, sector));
        return toCordView(cord);
    }

    /** Moving a cord to another sector moves its routes with it, and nothing else changes. */
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public CordView updateCord(UUID actorId, UUID cordId, CordSettings settings) {
        var actor = load(actorId);
        var cord =
                cords.findById(cordId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No cord"));
        var sector = loadSector(settings.sectorId());
        checkCordNumber(settings.number(), cordId);
        var before = describe(cord, loadSector(cord.sectorId()));
        cord.update(settings.number(), sector.id(), settings.active());
        auditLog.record(
                actor,
                AuditAction.CLUB_CORD_UPDATED,
                AuditTargetType.CORD,
                cord.id(),
                cordLabel(cord),
                before,
                describe(cord, sector));
        return toCordView(cord);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public HoldColorView createHoldColor(UUID actorId, HoldColorSettings settings) {
        var actor = load(actorId);
        checkHoldColorLabel(settings.label(), NO_ID);
        var last = holdColors.findAll().stream().mapToInt(HoldColor::position).max().orElse(0);
        var color =
                holdColors.save(
                        new HoldColor(
                                settings.label(),
                                settings.hex(),
                                settings.multicolor(),
                                settings.active(),
                                last + 1));
        auditLog.record(
                actor,
                AuditAction.CLUB_HOLD_COLOR_CREATED,
                AuditTargetType.HOLD_COLOR,
                color.id(),
                color.label(),
                null,
                describe(color));
        return toHoldColorView(color);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public HoldColorView updateHoldColor(
            UUID actorId, UUID holdColorId, HoldColorSettings settings) {
        var actor = load(actorId);
        var color =
                holdColors
                        .findById(holdColorId)
                        .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No colour"));
        checkHoldColorLabel(settings.label(), holdColorId);
        var before = describe(color);
        color.update(settings.label(), settings.hex(), settings.multicolor(), settings.active());
        auditLog.record(
                actor,
                AuditAction.CLUB_HOLD_COLOR_UPDATED,
                AuditTargetType.HOLD_COLOR,
                color.id(),
                color.label(),
                before,
                describe(color));
        return toHoldColorView(color);
    }

    private void checkSectorName(String name, UUID excludedId) {
        if (sectors.nameTakenByAnother(name.strip(), excludedId)) {
            throw new BusinessException(ErrorCode.ALREADY_EXISTS, "Sector name taken");
        }
    }

    private void checkCordNumber(int number, UUID excludedId) {
        if (number < Cord.MIN_NUMBER || number > Cord.MAX_NUMBER) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "Cord number " + number);
        }
        if (cords.numberTakenByAnother(number, excludedId)) {
            throw new BusinessException(ErrorCode.ALREADY_EXISTS, "Cord number taken");
        }
    }

    private void checkHoldColorLabel(String label, UUID excludedId) {
        if (holdColors.labelTakenByAnother(label.strip(), excludedId)) {
            throw new BusinessException(ErrorCode.ALREADY_EXISTS, "Hold colour label taken");
        }
    }

    private Sector loadSector(UUID sectorId) {
        return sectors.findById(sectorId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No sector"));
    }

    private Member load(UUID memberId) {
        return members.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "No member"));
    }

    /** Short, stable texts for the audit log. */
    private static String describe(Sector sector) {
        return sector.name()
                + (sector.description() == null ? "" : " (" + sector.description() + ")")
                + ", position "
                + sector.position();
    }

    private static String describe(Cord cord, Sector sector) {
        return "cord "
                + cord.number()
                + " in "
                + sector.name()
                + ", "
                + (cord.active() ? "active" : "inactive");
    }

    private static String describe(HoldColor color) {
        return color.label()
                + " "
                + color.hex()
                + (color.multicolor() ? ", multicolour" : "")
                + ", "
                + (color.active() ? "active" : "inactive");
    }

    private static String cordLabel(Cord cord) {
        return "Corde " + cord.number();
    }

    private static String names(Collection<Sector> all) {
        return all.stream()
                .sorted(Comparator.comparingInt(Sector::position))
                .map(Sector::name)
                .collect(Collectors.joining(", "));
    }

    private static TopoConfigView toConfigView(ClubLayout layout) {
        var cordsBySector =
                layout.sortedCords().stream().collect(Collectors.groupingBy(Cord::sectorId));
        return new TopoConfigView(
                layout.sortedSectors().stream()
                        .map(
                                sector ->
                                        toSectorView(
                                                sector,
                                                cordsBySector.getOrDefault(sector.id(), List.of())))
                        .toList(),
                layout.sortedHoldColors().stream().map(ClubConfigService::toHoldColorView).toList(),
                layout.sortedGrades().stream().map(ClubConfigService::toGradeView).toList());
    }

    private static SectorView toSectorView(Sector sector, List<Cord> sectorCords) {
        return new SectorView(
                sector.id(),
                sector.name(),
                sector.position(),
                sector.description(),
                sectorCords.stream()
                        .sorted(Comparator.comparingInt(Cord::number))
                        .map(ClubConfigService::toCordView)
                        .toList());
    }

    private static CordView toCordView(Cord cord) {
        return new CordView(cord.id(), cord.number(), cord.sectorId(), cord.active());
    }

    private static HoldColorView toHoldColorView(HoldColor color) {
        return new HoldColorView(
                color.id(),
                color.label(),
                color.hex(),
                color.multicolor(),
                color.active(),
                color.position());
    }

    private static GradeView toGradeView(Grade grade) {
        return new GradeView(grade.id(), grade.code(), grade.rank());
    }
}
