package com.enigmabridge.retry;

import org.json.JSONObject;

/**
 * EB retry strategy interface.
 *
 * Created by dusanklinec on 21.07.16.
 */
public interface EBRetryStrategy {
    String getName();

    void onFail();
    void onSuccess();
    void reset();
    boolean shouldContinue();
    long getWaitMilli();

    EBRetryStrategy copy();
    JSONObject toJSON(JSONObject json);
}
