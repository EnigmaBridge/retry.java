package com.enigmabridge.retry;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Simple retry strategy, with threshold, no waiting.
 *
 * Created by dusanklinec on 21.07.16.
 */
public class EBRetryStrategySimple implements EBRetryStrategy {
    protected int maxAttempts;
    protected int attempts = 0;

    /** Indicates that no more retries should be made for use in {@link #getWaitMilli()}. */
    static final long STOP = -1L;
    static final long NOWAIT = 0L;

    public static final String NAME = "simple";
    protected static final String FIELD_MAX_ATTEMPTS = "maxAttempts";

    public EBRetryStrategySimple(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    public EBRetryStrategySimple() {
    }

    public EBRetryStrategySimple(JSONObject json) {
        fromJSON(json);
    }

    @Override
    public void onFail() {
        attempts += 1;
    }

    @Override
    public void onSuccess() {
        //
    }

    @Override
    public void reset() {
        attempts = 0;
    }

    @Override
    public boolean shouldContinue() {
        return maxAttempts < 0 || attempts < maxAttempts;
    }

    @Override
    public long getWaitMilli() {
        return NOWAIT;
    }

    @Override
    public JSONObject toJSON(JSONObject json) {
        if (json == null){
            json = new JSONObject();
        }

        json.put(FIELD_MAX_ATTEMPTS, maxAttempts);
        return json;
    }

    protected void fromJSON(JSONObject json) throws JSONException {
        if (json == null){
            return;
        }

        if (json.has(FIELD_MAX_ATTEMPTS)){
            maxAttempts = EBUtils.getAsInteger(json, FIELD_MAX_ATTEMPTS, 10);
        }
    }

    @Override
    public EBRetryStrategy copy() {
        return new EBRetryStrategySimple(maxAttempts);
    }

    public int getMaxAttempts() {
        return maxAttempts;
    }

    public void setMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
    }

    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public String toString() {
        return "EBRetryStrategySimple{" +
                "maxAttempts=" + maxAttempts +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        EBRetryStrategySimple that = (EBRetryStrategySimple) o;

        return maxAttempts == that.maxAttempts;

    }

    @Override
    public int hashCode() {
        return maxAttempts;
    }
}
