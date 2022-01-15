package io.github.jiashunx.masker.rest.framework.exception;

public class MRestRuntimeException extends MRuntimeException {

    public MRestRuntimeException() {
        super();
    }

    public MRestRuntimeException(String message) {
        super(message);
    }

    public MRestRuntimeException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public MRestRuntimeException(Throwable throwable) {
        super(throwable);
    }

}
