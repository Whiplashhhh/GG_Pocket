package fr.gravelinesgrimp.pocket.platform.infra;

import fr.gravelinesgrimp.pocket.platform.domain.InstanceMarker;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InstanceMarkerRepository extends JpaRepository<InstanceMarker, Short> {}
