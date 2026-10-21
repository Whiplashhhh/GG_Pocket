package fr.gravelinesgrimp.testsupport;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Test-only endpoints that fail on purpose. Lives outside the application package so component
 * scanning never picks it up (it would leak into the OpenAPI contract); tests import it explicitly.
 */
@RestController
@RequestMapping("/probe")
public class ErrorProbeController {

    public record ProbeRequest(@NotBlank String name, @Min(1) int count) {}

    @PostMapping("/validated")
    public ProbeRequest validated(@Valid @RequestBody ProbeRequest request) {
        return request;
    }

    @GetMapping("/business")
    public void business() {
        throw new BusinessException(ErrorCode.ACCESS_DENIED, "Only the president can do that.");
    }

    @GetMapping("/denied")
    public void denied() {
        throw new AccessDeniedException("Denied by a method security rule");
    }

    @GetMapping("/stale")
    public void stale() {
        throw new OptimisticLockingFailureException("Route was updated by another transaction");
    }

    @GetMapping("/boom")
    public void boom() {
        throw new IllegalStateException("database password is hunter2");
    }
}
