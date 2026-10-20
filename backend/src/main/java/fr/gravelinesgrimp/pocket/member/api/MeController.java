package fr.gravelinesgrimp.pocket.member.api;

import fr.gravelinesgrimp.pocket.member.application.AvatarService;
import fr.gravelinesgrimp.pocket.member.application.MyProfileService;
import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "Me")
class MeController {

    private final MyProfileService profiles;
    private final AvatarService avatars;

    MeController(MyProfileService profiles, AvatarService avatars) {
        this.profiles = profiles;
        this.avatars = avatars;
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "getMyProfile", summary = "My whole profile")
    MyProfileResponse get(@Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me) {
        return MyProfileResponse.from(profiles.get(me.memberId()));
    }

    @PutMapping(path = "/profile", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "updateMyProfile", summary = "Edit my profile")
    MyProfileResponse update(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @Valid @RequestBody ProfileUpdateRequest request) {
        return MyProfileResponse.from(profiles.update(me.memberId(), request.toChange()));
    }

    @PutMapping(
            path = "/avatar",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            operationId = "replaceMyAvatar",
            summary = "Upload my profile photo (JPEG, PNG or WebP, 10 MB max)")
    MyProfileResponse replaceAvatar(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            @RequestPart("file") MultipartFile file)
            throws IOException {
        return MyProfileResponse.from(avatars.replace(me.memberId(), file.getBytes()));
    }

    @DeleteMapping("/avatar")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "removeMyAvatar", summary = "Remove my profile photo")
    void removeAvatar(@Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me) {
        avatars.remove(me.memberId());
    }
}
