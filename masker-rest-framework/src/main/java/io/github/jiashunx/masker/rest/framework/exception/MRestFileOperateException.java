package io.github.jiashunx.masker.rest.framework.exception;

/**
 * @author jiashunx
 */
public class MRestFileOperateException extends MRestServerException {

    public MRestFileOperateException() {
        super();
    }

    public MRestFileOperateException(String message) {
        super(message);
    }

    public MRestFileOperateException(String message, Throwable throwable) {
        super(message, throwable);
    }

    public MRestFileOperateException(Throwable throwable) {
        super(throwable);
    }
    
}
