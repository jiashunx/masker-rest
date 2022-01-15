package io.github.jiashunx.masker.rest.rsa.exception;

public class MRSAEncryptException extends MRSACommonException {

    public MRSAEncryptException() {}

    public MRSAEncryptException(String message) {
        super(message);
    }

    public MRSAEncryptException(Throwable throwable) {
        super(throwable);
    }

    public MRSAEncryptException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
