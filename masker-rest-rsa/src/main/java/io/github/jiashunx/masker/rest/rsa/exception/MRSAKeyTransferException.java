package io.github.jiashunx.masker.rest.rsa.exception;

public class MRSAKeyTransferException extends MRSACommonException {

    public MRSAKeyTransferException() {}

    public MRSAKeyTransferException(String message) {
        super(message);
    }

    public MRSAKeyTransferException(Throwable throwable) {
        super(throwable);
    }

    public MRSAKeyTransferException(String message, Throwable throwable) {
        super(message, throwable);
    }

}
