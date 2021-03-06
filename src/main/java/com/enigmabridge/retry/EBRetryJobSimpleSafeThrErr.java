package com.enigmabridge.retry;

/**
 * Retry Job implementation with job error = Throwable.
 *
 * Moreover all uncaught exceptions thrown in the job body are caught and considered as fail().
 *
 * Created by dusanklinec on 25.07.16.
 */
public abstract class EBRetryJobSimpleSafeThrErr<Result> extends EBRetryJobSimpleSafe<Result, Throwable> {
    @Override
    public void runAsync(EBCallback<Result, Throwable> callback) {
        try {
            runAsyncNoException(callback);
        } catch(Throwable th){
            callback.onFail(new EBRetryJobErrorThr(th), true);
        }
    }

}
