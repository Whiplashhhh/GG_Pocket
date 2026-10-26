package fr.gravelinesgrimp.pocket.member.infra;

import fr.gravelinesgrimp.pocket.member.domain.MediaFile;
import fr.gravelinesgrimp.pocket.member.domain.MediaUsage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public interface MediaFileRepository extends JpaRepository<MediaFile, UUID> {

    List<MediaFile> findAllByOwnerIdAndUsageIn(UUID ownerId, Collection<MediaUsage> usages);
}
