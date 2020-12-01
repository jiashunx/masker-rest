package io.github.jiashunx.masker.rest.framework.exception;

public class MRestFileDownloadException extends MRestServerException {

    public MRestFileDownloadException() {
        super();
    }

    public MRestFileDownloadException(String message) {
        super(message);
    }

    public MRestFileDownloadException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public MRestFileDownloadException(Throwable throwable) {
        super(throwable);
    }

}
