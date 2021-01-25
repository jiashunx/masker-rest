package io.github.jiashunx.masker.rest.framework.exception;

/**
 * @author jiashunx
 */
public class MRestMappingException extends MRestServerException {

    public MRestMappingException() {
        super();
    }

    public MRestMappingException(String message) {
        super(message);
    }

    public MRestMappingException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public MRestMappingException(Throwable throwable) {
        super(throwable);
    }

}
