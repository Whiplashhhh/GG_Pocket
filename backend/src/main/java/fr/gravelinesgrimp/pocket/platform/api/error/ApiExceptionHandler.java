package fr.gravelinesgrimp.pocket.platform.api.error;

import fr.gravelinesgrimp.pocket.platform.domain.BusinessException;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCategory;
import fr.gravelinesgrimp.pocket.platform.domain.ErrorCode;
import fr.gravelinesgrimp.pocket.platform.domain.RateLimitedException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.csrf.CsrfException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

/**
 * Turns every error into a Problem Details response (RFC 9457) carrying a stable {@code code} (see
 * {@link ErrorCode}). Also used by the security filter chain for 401 and 403 responses.
 */
@RestControllerAdvice
public class ApiExceptionHandler extends ResponseEntityExceptionHandler {

    static final String CODE_PROPERTY = "code";
    static final String ERRORS_PROPERTY = "errors";

    private static final Logger log = LoggerFactory.getLogger(ApiExceptionHandler.class);

    /** Framework errors (validation, unreadable body, 404, 405…): add our code. */
    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex,
            Object body,
            HttpHeaders headers,
            HttpStatusCode statusCode,
            WebRequest request) {
        var response = super.handleExceptionInternal(ex, body, headers, statusCode, request);
        if (response != null && response.getBody() instanceof ProblemDetail problem) {
            problem.setProperty(CODE_PROPERTY, frameworkErrorCode(ex, statusCode).name());
            if (ex instanceof MethodArgumentNotValidException invalid) {
                problem.setProperty(ERRORS_PROPERTY, fieldViolations(invalid));
            }
        }
        return response;
    }

    /**
     * The message of a business exception may name members or ids: it goes to the logs only. The
     * client gets the stable code, which the frontend turns into a sentence.
     */
    @ExceptionHandler(BusinessException.class)
    ResponseEntity<ProblemDetail> handleBusiness(BusinessException ex) {
        log.info("Business rule refused the request ({}): {}", ex.code(), ex.getMessage());
        return problem(
                statusOf(ex.code().category()),
                ex.code(),
                "The request was refused by a business rule.");
    }

    /** Too many attempts: says when to come back, in seconds (Retry-After). */
    @ExceptionHandler(RateLimitedException.class)
    ResponseEntity<ProblemDetail> handleRateLimited(RateLimitedException ex) {
        log.info("Rate limit reached: {}", ex.getMessage());
        var problem =
                ProblemDetail.forStatusAndDetail(
                        HttpStatus.TOO_MANY_REQUESTS, "Too many attempts, try again later.");
        problem.setProperty(CODE_PROPERTY, ex.code().name());
        var retryAfterSeconds = Math.max(1, ex.retryAfter().toSeconds());
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, Long.toString(retryAfterSeconds))
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    /**
     * Two people changed the same thing at the same moment: the stale version lost (optimistic
     * lock) or the other one took the unique name first. The loser is asked to reload, instead of
     * getting a server error. Services translate the conflicts they expect into precise codes.
     */
    @ExceptionHandler({
        OptimisticLockingFailureException.class,
        DataIntegrityViolationException.class
    })
    ResponseEntity<ProblemDetail> handleConcurrentChange(DataAccessException ex) {
        log.warn("Concurrent change refused: {}", ex.getMessage());
        return problem(
                HttpStatus.CONFLICT,
                ErrorCode.CONCURRENT_CHANGE,
                "Someone changed this at the same time: reload and try again.");
    }

    @ExceptionHandler(AuthenticationException.class)
    ResponseEntity<ProblemDetail> handleAuthentication(AuthenticationException ex) {
        return unauthenticated();
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ProblemDetail> handleAccessDenied(AccessDeniedException ex) {
        if (ex instanceof CsrfException) {
            return problem(
                    HttpStatus.FORBIDDEN,
                    ErrorCode.CSRF_TOKEN_INVALID,
                    "Missing or invalid CSRF token.");
        }
        if (isAnonymous()) {
            return unauthenticated();
        }
        return problem(
                HttpStatus.FORBIDDEN,
                ErrorCode.ACCESS_DENIED,
                "You are not allowed to perform this action.");
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ProblemDetail> handleUnexpected(Exception ex) {
        log.error("Unexpected error while handling a request", ex);
        return problem(
                HttpStatus.INTERNAL_SERVER_ERROR,
                ErrorCode.INTERNAL_ERROR,
                "Something went wrong on our side.");
    }

    private static ResponseEntity<ProblemDetail> unauthenticated() {
        return problem(
                HttpStatus.UNAUTHORIZED,
                ErrorCode.UNAUTHENTICATED,
                "You need to be logged in to do this.");
    }

    private static ResponseEntity<ProblemDetail> problem(
            HttpStatus status, ErrorCode code, String detail) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setProperty(CODE_PROPERTY, code.name());
        return ResponseEntity.status(status)
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(problem);
    }

    private static boolean isAnonymous() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null
                || authentication instanceof AnonymousAuthenticationToken
                || !authentication.isAuthenticated();
    }

    private static ErrorCode frameworkErrorCode(Exception ex, HttpStatusCode status) {
        if (ex instanceof MethodArgumentNotValidException
                || ex instanceof HandlerMethodValidationException) {
            return ErrorCode.VALIDATION_FAILED;
        }
        return switch (status.value()) {
            case 400 -> ErrorCode.MALFORMED_REQUEST;
            case 401 -> ErrorCode.UNAUTHENTICATED;
            case 403 -> ErrorCode.ACCESS_DENIED;
            case 404 -> ErrorCode.NOT_FOUND;
            case 405 -> ErrorCode.METHOD_NOT_ALLOWED;
            case 413 -> ErrorCode.PAYLOAD_TOO_LARGE;
            case 415 -> ErrorCode.UNSUPPORTED_MEDIA_TYPE;
            default ->
                    status.is5xxServerError()
                            ? ErrorCode.INTERNAL_ERROR
                            : ErrorCode.REQUEST_REJECTED;
        };
    }

    private static HttpStatus statusOf(ErrorCategory category) {
        return switch (category) {
            case INVALID, PROTOCOL -> HttpStatus.BAD_REQUEST;
            case UNAUTHENTICATED -> HttpStatus.UNAUTHORIZED;
            case FORBIDDEN -> HttpStatus.FORBIDDEN;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case TOO_MANY_REQUESTS -> HttpStatus.TOO_MANY_REQUESTS;
            case INTERNAL -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }

    private static List<FieldViolation> fieldViolations(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .map(error -> new FieldViolation(error.getField(), error.getCode()))
                .toList();
    }
}
