package io.github.jiashunx.masker.rest.framework.exception;

/**
 * @author jiashunx
 */
public class MRestServerCloseException extends MRestServerException {

    public MRestServerCloseException() {
        super();
    }

    public MRestServerCloseException(String message) {
        super(message);
    }

    public MRestServerCloseException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public MRestServerCloseException(Throwable throwable) {
        super(throwable);
    }

}
