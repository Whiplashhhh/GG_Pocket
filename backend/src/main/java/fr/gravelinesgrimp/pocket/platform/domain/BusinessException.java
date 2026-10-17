package fr.gravelinesgrimp.pocket.platform.domain;

/**
 * A business rule refused the operation. Turned into a Problem Details response carrying {@link
 * #code()}; the message is meant for developers and logs, never shown as-is to members.
 */
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final ErrorCode code;

    public BusinessException(ErrorCode code, String message) {
        super(message);
        this.code = code;
    }

    public ErrorCode code() {
        return code;
    }
}
