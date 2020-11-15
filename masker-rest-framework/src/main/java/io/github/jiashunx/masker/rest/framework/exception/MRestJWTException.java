package io.github.jiashunx.masker.rest.framework.exception;

/**
 * @author jiashunx
 */
public class MRestJWTException extends MRestServerException {

    public MRestJWTException() {
        super();
    }

    public MRestJWTException(String message) {
        super(message);
    }

    public MRestJWTException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public MRestJWTException(Throwable throwable) {
        super(throwable);
    }


}
