package io.github.jiashunx.masker.rest.rsa.exception;

import io.github.jiashunx.masker.rest.framework.exception.MRuntimeException;

public class MRSACommonException extends MRuntimeException {

    public MRSACommonException() {}

    public MRSACommonException(String message) {
        super(message);
    }

    public MRSACommonException(Throwable throwable) {
        super(throwable);
    }

    public MRSACommonException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
