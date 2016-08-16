package com.enigmabridge.retry;

/**
 * Job error if error = throwable. Special case, although quite common.
 *
 * Created by dusanklinec on 25.07.16.
 */
public class EBRetryJobErrorThr extends EBRetryJobError<Throwable> {
    public EBRetryJobErrorThr(Throwable throwable) {
        super(throwable, throwable);
    }
}
