package com.enigmabridge.retry;

/**
 * Retry exception - retry mechanism aborted, typically with fatal error.
 *
 * Created by dusanklinec on 21.07.16.
 */
public class EBRetryAbortedException extends EBRetryException {
    public EBRetryAbortedException() {
    }

    public EBRetryAbortedException(String message) {
        super(message);
    }

    public EBRetryAbortedException(String message, Throwable cause) {
        super(message, cause);
    }

    public EBRetryAbortedException(Throwable cause) {
        super(cause);
    }

    public EBRetryAbortedException(Object error, EBRetry retry) {
        super(error, retry);
    }

    public EBRetryAbortedException(String message, Object error, EBRetry retry) {
        super(message, error, retry);
    }

    public EBRetryAbortedException(String message, Throwable cause, Object error, EBRetry retry) {
        super(message, cause, error, retry);
    }

    public EBRetryAbortedException(Throwable cause, Object error, EBRetry retry) {
        super(cause, error, retry);
    }
}
