package fr.gravelinesgrimp.pocket.platform.api.meta;

import fr.gravelinesgrimp.pocket.platform.application.AppMetadataService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/meta")
@Tag(name = "Meta")
class MetaController {

    private final AppMetadataService metadataService;

    MetaController(AppMetadataService metadataService) {
        this.metadataService = metadataService;
    }

    @GetMapping
    @Operation(
            operationId = "getMeta",
            summary = "Version, environment and commit of the running server (public)")
    MetaResponse meta() {
        var metadata = metadataService.current();
        return new MetaResponse(
                metadata.version(),
                metadata.environment().code(),
                metadata.commit(),
                metadata.branch());
    }
}
