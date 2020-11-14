package io.github.jiashunx.masker.rest.framework.exception;

/**
 * @author jiashunx
 */
public class MRestHandleException extends MRestServerException {

    public MRestHandleException() {
        super();
    }

    public MRestHandleException(String message) {
        super(message);
    }

    public MRestHandleException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public MRestHandleException(Throwable throwable) {
        super(throwable);
    }

}
