package io.github.jiashunx.masker.rest.framework.exception;

/**
 * @author jiashunx
 */
public class MRestSerializeException extends MRestServerException {

    public MRestSerializeException() {
        super();
    }

    public MRestSerializeException(String message) {
        super(message);
    }

    public MRestSerializeException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public MRestSerializeException(Throwable throwable) {
        super(throwable);
    }

}
