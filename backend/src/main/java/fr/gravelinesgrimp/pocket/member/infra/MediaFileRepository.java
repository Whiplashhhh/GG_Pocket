package fr.gravelinesgrimp.pocket.member.infra;

import fr.gravelinesgrimp.pocket.member.domain.MediaFile;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface MediaFileRepository extends JpaRepository<MediaFile, UUID> {}
