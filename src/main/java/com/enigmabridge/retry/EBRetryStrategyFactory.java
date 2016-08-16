package com.enigmabridge.retry;

import org.json.JSONObject;

/**
 * Construct retry strategy from serialized data.
 *
 * Created by dusanklinec on 21.07.16.
 */
public class EBRetryStrategyFactory {
    private static final String FIELD_RETRY_NAME = "name";
    private static final String FIELD_RETRY_DATA = "data";

    public static EBRetryStrategy getByName(String name){
        return getByName(name, null);
    }

    public static EBRetryStrategy getByName(String name, JSONObject config){
        if (EBRetryStrategyBackoff.NAME.equals(name)){
            return new EBRetryStrategyBackoff(config);

        } else if (EBRetryStrategySimple.NAME.equals(name)) {
            return new EBRetryStrategySimple(config);

        } else {
            throw new IllegalArgumentException("Unknown strategy type");
        }
    }

    public static EBRetryStrategy fromJSON(JSONObject json){
        if (!json.has(FIELD_RETRY_NAME)){
            return null;
        }

        final JSONObject config = json.has(FIELD_RETRY_DATA) ? json.getJSONObject(FIELD_RETRY_DATA) : null;
        return getByName(json.getString(FIELD_RETRY_NAME), config);
    }

    public static JSONObject toJSON(EBRetryStrategy strategy, JSONObject json){
        if (strategy == null){
            return json;
        }

        if (json == null){
            json = new JSONObject();
        }

        json.put(FIELD_RETRY_NAME, strategy.getName());
        json.put(FIELD_RETRY_DATA, strategy.toJSON(null));
        return json;
    }

}
