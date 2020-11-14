package io.github.jiashunx.masker.rest.framework.exception;

/**
 * @author jiashunx
 */
public class MRestServerException extends RuntimeException {

    public MRestServerException() {
        super();
    }

    public MRestServerException(String message) {
        super(message);
    }

    public MRestServerException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public MRestServerException(Throwable throwable) {
        super(throwable);
    }

}
