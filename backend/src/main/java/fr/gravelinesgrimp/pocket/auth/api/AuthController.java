package fr.gravelinesgrimp.pocket.auth.api;

import fr.gravelinesgrimp.pocket.auth.application.LoginService;
import fr.gravelinesgrimp.pocket.auth.application.PasswordResetService;
import fr.gravelinesgrimp.pocket.auth.application.RegistrationService;
import fr.gravelinesgrimp.pocket.member.api.CurrentMemberResponse;
import fr.gravelinesgrimp.pocket.member.application.MyProfileService;
import fr.gravelinesgrimp.pocket.member.domain.MemberPrincipal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Signing in and out, registration and password reset: the public doors of the app. */
@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Auth")
class AuthController {

    private final LoginService loginService;
    private final RegistrationService registrationService;
    private final PasswordResetService passwordResetService;
    private final MyProfileService profiles;
    private final SessionEstablisher sessions;

    AuthController(
            LoginService loginService,
            RegistrationService registrationService,
            PasswordResetService passwordResetService,
            MyProfileService profiles,
            SessionEstablisher sessions) {
        this.loginService = loginService;
        this.registrationService = registrationService;
        this.passwordResetService = passwordResetService;
        this.profiles = profiles;
        this.sessions = sessions;
    }

    @GetMapping(path = "/state", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            operationId = "getAuthState",
            summary = "Whether this browser is signed in (public). Also sets the CSRF cookie.")
    AuthStateResponse state(
            @Parameter(hidden = true) @AuthenticationPrincipal MemberPrincipal me,
            HttpServletRequest request) {
        // The CSRF token is created lazily: reading it here sets the XSRF-TOKEN cookie, which the
        // app sends back in a header on every write.
        if (request.getAttribute(CsrfToken.class.getName()) instanceof CsrfToken csrfToken) {
            csrfToken.getToken();
        }
        if (me == null) {
            return new AuthStateResponse(false, null);
        }
        return new AuthStateResponse(
                true, CurrentMemberResponse.from(profiles.current(me.memberId())));
    }

    @PostMapping(path = "/login", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "login", summary = "Sign in with nickname and password")
    CurrentMemberResponse login(
            @Valid @RequestBody LoginRequest body,
            HttpServletRequest request,
            HttpServletResponse response) {
        var member =
                loginService.authenticate(
                        body.nickname(), body.password(), ClientAddress.of(request));
        sessions.signIn(member, body.rememberMe(), request, response);
        return CurrentMemberResponse.from(member);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(operationId = "logout", summary = "Close the session of this device")
    void logout(HttpServletRequest request, HttpServletResponse response) {
        sessions.signOut(request);
        // A shared phone keeps nothing of this member: cached photos included.
        response.setHeader("Clear-Site-Data", "\"cache\"");
    }

    @PostMapping("/registration-code/check")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            operationId = "checkRegistrationCode",
            summary = "Whether a registration code can be used (204) or not (400)")
    void checkRegistrationCode(
            @Valid @RequestBody RegistrationCodeCheckRequest body, HttpServletRequest request) {
        registrationService.checkCode(body.code(), ClientAddress.of(request));
    }

    @PostMapping(path = "/register", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "register", summary = "Join the club, then sign in at once")
    CurrentMemberResponse register(
            @Valid @RequestBody RegisterRequest body,
            HttpServletRequest request,
            HttpServletResponse response) {
        var member = registrationService.register(body.toRegistration(), ClientAddress.of(request));
        sessions.signIn(member, body.rememberMe(), request, response);
        return CurrentMemberResponse.from(member);
    }

    @PostMapping(path = "/password-reset/check", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(operationId = "checkPasswordResetLink", summary = "Who a reset link is for")
    PasswordResetTargetResponse checkPasswordReset(
            @Valid @RequestBody PasswordResetCheckRequest body, HttpServletRequest request) {
        var target = passwordResetService.check(body.token(), ClientAddress.of(request));
        return new PasswordResetTargetResponse(target.firstName(), target.nickname());
    }

    @PostMapping(path = "/password-reset", produces = MediaType.APPLICATION_JSON_VALUE)
    @Operation(
            operationId = "resetPassword",
            summary = "Choose a new password with a reset link; signs in this device only")
    CurrentMemberResponse resetPassword(
            @Valid @RequestBody PasswordResetRequest body,
            HttpServletRequest request,
            HttpServletResponse response) {
        var member =
                passwordResetService.complete(
                        body.token(), body.newPassword(), ClientAddress.of(request));
        sessions.signIn(member, true, request, response);
        return CurrentMemberResponse.from(member);
    }
}
