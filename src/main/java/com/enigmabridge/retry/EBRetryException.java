package com.enigmabridge.retry;

/**
 * Root exception for all retry based exceptions.
 *
 * Created by dusanklinec on 21.07.16.
 */
public class EBRetryException extends Exception {
    /**
     * Error indicated in onFail() notify.
     * Mostly it is an exception that caused job to fail.
     */
    protected Object error;

    /**
     * Retry mechanism throwing this exception.
     */
    protected EBRetry retry;

    public EBRetryException() {
    }

    public EBRetryException(String message) {
        super(message);
    }

    public EBRetryException(String message, Throwable cause) {
        super(message, cause);
    }

    public EBRetryException(Throwable cause) {
        super(cause);
    }

    public EBRetryException(Object error, EBRetry retry) {
        this.error = error;
        this.retry = retry;
        updateCauseIfApplicable();
    }

    public EBRetryException(String message, Object error, EBRetry retry) {
        super(message);
        this.error = error;
        this.retry = retry;
        updateCauseIfApplicable();
    }

    public EBRetryException(String message, Throwable cause, Object error, EBRetry retry) {
        super(message, cause);
        this.error = error;
        this.retry = retry;
    }

    public EBRetryException(Throwable cause, Object error, EBRetry retry) {
        super(cause);
        this.error = error;
        this.retry = retry;
    }

    public Object getError() {
        return error;
    }

    public EBRetry getRetry() {
        return retry;
    }

    protected void updateCauseIfApplicable(){
        final Throwable cause = getErrorCauseIfAny();
        if (cause != null){
            initCause(cause);
        }
    }

    /**
     * Return an trhowable instance if the error object is of Throwable type.
     * Null otherwise.
     *
     * @return Throwable or null
     */
    public Throwable getErrorCauseIfAny(){
        if (error == null){
            return null;
        }

        if (error instanceof Throwable){
            return (Throwable) error;
        }

        if (this.error instanceof EBRetryJobErrorThr){
            return ((EBRetryJobErrorThr) this.error).getThrowable();
        }

        if (this.error instanceof EBRetryJobError){
            return ((EBRetryJobError) this.error).getThrowable();
        }

        return null;
    }

    /**
     * Overridden getCause that takes error object into account also.
     * Original cause has the precedence, if null, error object is tested if is Throwable instance.
     *
     * @return cause or null
     */
    @Override
    public synchronized Throwable getCause() {
        final Throwable cause = super.getCause();
        if (cause != null){
            return cause;
        }

        return getErrorCauseIfAny();
    }
}
