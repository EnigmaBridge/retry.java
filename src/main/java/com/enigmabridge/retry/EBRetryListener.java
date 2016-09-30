package com.enigmabridge.retry;

/**
 * Listener for Retry mechanism to listen for job retry status.
 * Listener is notified if job succeeds or fails.
 * It is added to the list of listeners in the EBRetry.
 *
 * Created by dusanklinec on 21.07.16.
 */
public interface EBRetryListener<Result, Error> {
    /**
     * Called by job on success.
     *
     * @param result Result
     * @param retry pair EBRetry of Result and Error types
     */
    void onSuccess(Result result, EBRetry<Result, Error> retry);

    /**
     * Called by job on fail.
     * @param error EBRetryJobError
     * @param retry pair EBRetry of Result and Error types
     */
    void onFail(EBRetryJobError<Error> error, EBRetry<Result, Error> retry);
}
