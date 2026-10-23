package fr.gravelinesgrimp.pocket.club.api;

import fr.gravelinesgrimp.pocket.club.application.ClubConfigService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/api/v1/topo/config", produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Topo")
class TopoConfigController {

    private final ClubConfigService service;

    TopoConfigController(ClubConfigService service) {
        this.service = service;
    }

    @GetMapping
    @Operation(operationId = "getTopoConfig", summary = "Sectors, cords, hold colours and grades")
    TopoConfigResponse get() {
        return TopoConfigResponse.from(service.config());
    }
}
