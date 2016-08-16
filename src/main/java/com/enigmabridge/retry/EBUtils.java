package com.enigmabridge.retry;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dusanklinec on 15.08.16.
 */
public class EBUtils {
    /**
     * Tries to extract json parameter as an string.
     * @param json target
     * @param key field name
     * @return extracted string
     * @throws JSONException - if the JSON object doesn't contain the item or is malformed
     */
    public static String tryGetAsString(JSONObject json, String key) throws JSONException {
        return json.getString(key);
    }

    /**
     * Tries to extract json parameter as an integer.
     * @param json target
     * @param key field name
     * @param radix radix for string / int conversion
     * @return extracted integer
     * @throws JSONException - if the JSON object doesn't contain the item or is malformed
     */
    public static Integer tryGetAsInteger(JSONObject json, String key, int radix) throws JSONException {
        final Object obj = json.get(key);

        if (obj instanceof String){
            try {
                return Integer.parseInt((String) obj, radix);
            } catch(Exception e){
                return null;
            }
        }

        try {
            return obj instanceof Number ? ((Number) obj).intValue() : (int) json.getDouble(key);
        } catch(Exception e){
            return null;
        }
    }

    public static int getAsInteger(JSONObject json, String key, int radix) throws JSONException {
        final Integer toret = tryGetAsInteger(json, key, radix);
        if (toret == null) {
            throw new JSONException("JSONObject[" + key + "] not found.");
        }

        return toret;
    }

    /**
     * Tries to extract json parameter as a long.
     * @param json target
     * @param key field name
     * @param radix radix for string / int conversion
     * @return extracted long
     * @throws JSONException - if the JSON object doesn't contain the item or is malformed
     */
    public static Long tryGetAsLong(JSONObject json, String key, int radix) throws JSONException {
        final Object obj = json.get(key);

        if (obj instanceof String){
            try {
                return Long.parseLong((String) obj, radix);
            } catch(Exception e){
                return null;
            }
        }

        try {
            return obj instanceof Number ? ((Number) obj).longValue() : (long) json.getDouble(key);
        } catch(Exception e){
            return null;
        }
    }

    public static long getAsLong(JSONObject json, String key, int radix) throws JSONException {
        final Long toret = tryGetAsLong(json, key, radix);
        if (toret == null) {
            throw new JSONException("JSONObject[" + key + "] not found.");
        }

        return toret;
    }

    /**
     * Tries to extract json parameter as a long.
     * @param json target
     * @param key field name
     * @return extracted long
     * @throws JSONException - if the JSON object doesn't contain the item or is malformed
     */
    public static Double tryGetAsDouble(JSONObject json, String key) throws JSONException {
        final Object obj = json.get(key);

        if (obj instanceof String){
            try {
                return Double.parseDouble((String) obj);
            } catch(Exception e){
                return null;
            }
        }

        try {
            return obj instanceof Number ? ((Number) obj).doubleValue() : json.getDouble(key);
        } catch(Exception e){
            return null;
        }
    }

    public static double getAsDouble(JSONObject json, String key) throws JSONException {
        final Double toret = tryGetAsDouble(json, key);
        if (toret == null) {
            throw new JSONException("JSONObject[" + key + "] not found.");
        }

        return toret;
    }
}
