package com.enigmabridge.retry;

/**
 * Future object, similar to {@link java.util.concurrent.Future}
 *
 * Used in async job invocations. Can be used to detect if task is running or
 * finished. Caller can cancel the task or skip current backoff waiting interval.
 *
 * Created by dusanklinec on 21.07.16.
 */
public interface EBFuture<Result, Error> {
    /**
     * Returns true if async task is still running.
     * @return true if running
     */
    boolean isRunning();

    /**
     * Returns true if async task finished its execution.
     * @return true if done
     */
    boolean isDone();

    /**
     * Triggers cancellation of the task.
     * Does not interrupt currently running job, disables invocation of the next attempt.
     */
    void cancel();

    /**
     * If task is currently waiting in a backoff interval, this interrupts waiting and
     * immediately executes the task - skipping the waiting interval.
     * Like "Retry now" button in UI.
     */
    void runNow();
}
