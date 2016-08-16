package com.enigmabridge.retry;

/**
 * General exception used as a cause that job failed, if there is no other exception cause.
 * Used as an error cause with conjunction to EBRetryFailedException.
 *
 * Created by dusanklinec on 25.07.16.
 */
public class EBRetryJobException extends Exception {
    /**
     * Error object - result of the job that is interpreted as an error.
     */
    Object error;

    public EBRetryJobException() {
    }

    public EBRetryJobException(String message) {
        super(message);
    }

    public EBRetryJobException(String message, Throwable cause) {
        super(message, cause);
    }

    public EBRetryJobException(Throwable cause) {
        super(cause);
    }

    public EBRetryJobException(Object error) {
        this.error = error;
    }

    public EBRetryJobException(String message, Object error) {
        super(message);
        this.error = error;
    }

    public EBRetryJobException(String message, Throwable cause, Object error) {
        super(message, cause);
        this.error = error;
    }

    public EBRetryJobException(Throwable cause, Object error) {
        super(cause);
        this.error = error;
    }

    public Object getError() {
        return error;
    }

    public void setError(Object error) {
        this.error = error;
    }

    /**
     * Return an trhowable instance if the error object is of Throwable type.
     * Null otherwise.
     *
     * @return Throwable or null
     */
    public Throwable getErrorCauseIfAny(){
        return error != null && error instanceof Throwable ? (Throwable) error : null;
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
