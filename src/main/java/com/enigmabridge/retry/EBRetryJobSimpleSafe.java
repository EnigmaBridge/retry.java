package com.enigmabridge.retry;

/**
 * Simple retry job implementation. All uncaught throwables are considered as fatal errors.
 *
 * Created by dusanklinec on 25.07.16.
 */
public abstract class EBRetryJobSimpleSafe<Result, Error> extends EBRetryJobSimple<Result, Error> {

    @Override
    public void runAsync(EBCallback<Result, Error> callback) {
        try {
            runAsyncNoException(callback);
        } catch(Throwable th){
            callback.onFail(new EBRetryJobError<Error>(th), true);
        }
    }

    /**
     * Job overrides this main method. If uncaught Throwable is thrown in the job, it is considered as a fatal error of the job.
     *
     * @param callback
     * @throws Throwable
     */
    public abstract void runAsyncNoException(EBCallback<Result, Error> callback) throws Throwable;
}
