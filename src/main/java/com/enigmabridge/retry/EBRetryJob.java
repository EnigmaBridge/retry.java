package com.enigmabridge.retry;

/**
 * Simple job - subject to retry mechanism.
 *
 * Created by dusanklinec on 21.07.16.
 */
public interface EBRetryJob<Result, Error> {
    /**
     * Executes the function.
     * Should be re-entrant, called multiple times, if previous attempt failed.
     *
     * @param callback callback to call
     */
    void runAsync(EBCallback<Result, Error> callback);

    /**
     * On retry signal to the job.
     *
     * @param retry EBRetry
     */
    void onRetry(EBRetry<Result, Error> retry);
}
