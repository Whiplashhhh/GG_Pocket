package fr.gravelinesgrimp.pocket.account.api;

import fr.gravelinesgrimp.pocket.account.application.MyDataExportService;
import fr.gravelinesgrimp.pocket.account.application.MyDataExportService.MyDataExport;
import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;
import fr.gravelinesgrimp.pocket.platform.domain.ClubTime;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;

/**
 * "Exporter mes données" (spec §14). The export is a file for the member, not a screen of the app:
 * it is sent as the service builds it, with no response type of its own.
 */
@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "Me")
class MyDataController {

    private final MyDataExportService export;
    private final Clock clock;

    MyDataController(MyDataExportService export, Clock clock) {
        this.export = export;
        this.clock = clock;
    }

    @GetMapping(path = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            operationId = "exportMyData",
            summary = "Everything the app keeps about me, as a file")
    ResponseEntity<MyDataExport> export(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me) {
        var name = "gg-pocket-mes-donnees-" + ClubTime.today(clock) + ".json";
        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(name).build().toString())
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(export.export(me.memberId()));
    }
}
