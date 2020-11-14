package io.github.jiashunx.masker.rest.framework.exception;

/**
 * @author jiashunx
 */
public class MRestServerInitializeException extends MRestServerException {

    public MRestServerInitializeException() {
        super();
    }

    public MRestServerInitializeException(String message) {
        super(message);
    }

    public MRestServerInitializeException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public MRestServerInitializeException(Throwable throwable) {
        super(throwable);
    }

}
