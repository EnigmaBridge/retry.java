package com.enigmabridge.retry;

/**
 * Simple callback interface for job to signalize its success or fail.
 *
 * Created by dusanklinec on 21.07.16.
 */
public interface EBCallback<Result, Error> {
    /**
     * Called by job on success.
     * @param result Result
     */
    void onSuccess(Result result);

    /**
     * Called by job on fail.
     * @param error EBRetryJobError
     * @param abort true if should terminate.
     */
    void onFail(EBRetryJobError<Error> error, boolean abort);
}
