package io.github.jiashunx.masker.rest.rsa.exception;

public class MRSASignatureException extends MRSACommonException {

    public MRSASignatureException() {}

    public MRSASignatureException(String message) {
        super(message);
    }

    public MRSASignatureException(Throwable throwable) {
        super(throwable);
    }

    public MRSASignatureException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
