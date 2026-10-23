package fr.gravelinesgrimp.pocket.topo.infra;

import fr.gravelinesgrimp.pocket.topo.domain.ClimbMode;
import fr.gravelinesgrimp.pocket.topo.domain.RouteCheck;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface RouteCheckRepository extends JpaRepository<RouteCheck, UUID> {

    Optional<RouteCheck> findByMemberIdAndRouteIdAndMode(
            UUID memberId, UUID routeId, ClimbMode mode);

    /** Callers never pass an empty collection. */
    List<RouteCheck> findAllByMemberIdAndRouteIdIn(UUID memberId, Collection<UUID> routeIds);
}
