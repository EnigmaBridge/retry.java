package com.enigmabridge.retry;

/**
 * Created by dusanklinec on 21.07.16.
 */
public interface EBFuture<Result, Error> {
    boolean isRunning();
    boolean isDone();
    void cancel();
    void runNow();
}
