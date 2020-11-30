package io.github.jiashunx.masker.rest.framework.exception;

/**
 * @author jiashunx
 */
public class MRestFileUploadException extends MRestServerException {

    public MRestFileUploadException() {
        super();
    }

    public MRestFileUploadException(String message) {
        super(message);
    }

    public MRestFileUploadException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public MRestFileUploadException(Throwable throwable) {
        super(throwable);
    }

}
