package com.enigmabridge.retry;

/**
 * Retry exception - retry mechanism was cancelled. Typically on user intent / timeouts, too many attempts, ...
 *
 * Created by dusanklinec on 21.07.16.
 */
public class EBRetryCancelledException extends EBRetryException {
    public EBRetryCancelledException() {
    }

    public EBRetryCancelledException(String message) {
        super(message);
    }

    public EBRetryCancelledException(String message, Throwable cause) {
        super(message, cause);
    }

    public EBRetryCancelledException(Throwable cause) {
        super(cause);
    }

    public EBRetryCancelledException(Object error, EBRetry retry) {
        super(error, retry);
    }

    public EBRetryCancelledException(String message, Object error, EBRetry retry) {
        super(message, error, retry);
    }

    public EBRetryCancelledException(String message, Throwable cause, Object error, EBRetry retry) {
        super(message, cause, error, retry);
    }

    public EBRetryCancelledException(Throwable cause, Object error, EBRetry retry) {
        super(cause, error, retry);
    }
}
