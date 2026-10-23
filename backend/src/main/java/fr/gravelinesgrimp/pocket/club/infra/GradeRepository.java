package fr.gravelinesgrimp.pocket.club.infra;

import fr.gravelinesgrimp.pocket.club.domain.Grade;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GradeRepository extends JpaRepository<Grade, UUID> {}
