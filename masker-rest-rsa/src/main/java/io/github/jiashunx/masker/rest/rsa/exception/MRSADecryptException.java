package io.github.jiashunx.masker.rest.rsa.exception;

public class MRSADecryptException extends MRSACommonException {

    public MRSADecryptException() {}

    public MRSADecryptException(String message) {
        super(message);
    }

    public MRSADecryptException(Throwable throwable) {
        super(throwable);
    }

    public MRSADecryptException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
