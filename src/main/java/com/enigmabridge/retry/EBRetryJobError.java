package com.enigmabridge.retry;

/**
 * Wrapper for the job error.
 *
 * Created by dusanklinec on 25.07.16.
 */
public class EBRetryJobError<Error> {
    /**
     * If job fails with the particular error, this is the error returned from the job.
     */
    protected Error error;

    /**
     * Job may also fail with throwable.
     */
    protected Throwable throwable;

    public EBRetryJobError() {
    }

    public EBRetryJobError(Error error) {
        this.error = error;
    }

    public EBRetryJobError(Throwable throwable) {
        this.throwable = throwable;
    }

    public EBRetryJobError(Error error, Throwable throwable) {
        this.error = error;
        this.throwable = throwable;
    }

    public Error getError() {
        return error;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setError(Error error) {
        this.error = error;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
