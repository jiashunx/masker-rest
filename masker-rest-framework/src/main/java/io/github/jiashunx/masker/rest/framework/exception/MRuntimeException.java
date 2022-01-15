package io.github.jiashunx.masker.rest.framework.exception;

public class MRuntimeException extends RuntimeException {

    public MRuntimeException() {
        super();
    }

    public MRuntimeException(String message) {
        super(message);
    }

    public MRuntimeException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public MRuntimeException(Throwable throwable) {
        super(throwable);
    }

}
