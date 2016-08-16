package com.enigmabridge.retry;

/**
 * Exception thrown when calling sync job.
 * Semantics: job failed for some reason. E.g., maximum number of attempts failed.
 * Underlying error is set.
 *
 * Created by dusanklinec on 21.07.16.
 */
public class EBRetryFailedException extends EBRetryException {
    public EBRetryFailedException() {
    }

    public EBRetryFailedException(String message) {
        super(message);
    }

    public EBRetryFailedException(String message, Throwable cause) {
        super(message, cause);
    }

    public EBRetryFailedException(Throwable cause) {
        super(cause);
    }

    public EBRetryFailedException(Object error, EBRetry retry) {
        super(error, retry);
    }

    public EBRetryFailedException(String message, Object error, EBRetry retry) {
        super(message, error, retry);
    }

    public EBRetryFailedException(String message, Throwable cause, Object error, EBRetry retry) {
        super(message, cause, error, retry);
    }

    public EBRetryFailedException(Throwable cause, Object error, EBRetry retry) {
        super(cause, error, retry);
    }

}
