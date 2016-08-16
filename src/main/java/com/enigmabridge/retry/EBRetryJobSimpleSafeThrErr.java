package com.enigmabridge.retry;

/**
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
