package com.enigmabridge.retry;

/*
 * Copyright (c) 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

import org.json.JSONObject;

import java.io.IOException;

/**
 * Implementation of {@link BackOff} that increases the back off period for each retry attempt using
 * a randomization function that grows exponentially.
 * <p>
 * <p>
 * {@link #nextBackOffMillis()} is calculated using the following formula:
 * </p>
 * <p>
 * <pre>
 * randomized_interval =
 * retry_interval * (random value in range [1 - randomization_factor, 1 + randomization_factor])
 * </pre>
 * <p>
 * <p>
 * In other words {@link #nextBackOffMillis()} will range between the randomization factor
 * percentage below and above the retry interval. For example, using 2 seconds as the base retry
 * interval and 0.5 as the randomization factor, the actual back off period used in the next retry
 * attempt will be between 1 and 3 seconds.
 * </p>
 * <p>
 * <p>
 * <b>Note:</b> max_interval caps the retry_interval and not the randomized_interval.
 * </p>
 * <p>
 * <p>
 * If the time elapsed since an {@link EBRetryStrategyBackoff} instance is created goes past the
 * max_elapsed_time then the method {@link #nextBackOffMillis()} starts returning
 * {@link BackOff#STOP}. The elapsed time can be reset by calling {@link #reset()}.
 * </p>
 * <p>
 * <p>
 * Example: The default retry_interval is .5 seconds, default randomization_factor is 0.5, default
 * multiplier is 1.5 and the default max_interval is 1 minute. For 10 tries the sequence will be
 * (values in seconds) and assuming we go over the max_elapsed_time on the 10th try:
 * </p>
 * <p>
 * <pre>
 * request#     retry_interval     randomized_interval
 *
 * 1             0.5                [0.25,   0.75]
 * 2             0.75               [0.375,  1.125]
 * 3             1.125              [0.562,  1.687]
 * 4             1.687              [0.8435, 2.53]
 * 5             2.53               [1.265,  3.795]
 * 6             3.795              [1.897,  5.692]
 * 7             5.692              [2.846,  8.538]
 * 8             8.538              [4.269, 12.807]
 * 9            12.807              [6.403, 19.210]
 * 10           19.210              {@link BackOff#STOP}
 * </pre>
 * <p>
 * <p>
 * Implementation is not thread-safe.
 * </p>
 *
 * @author Ravi Mistry
 * @since 1.15
 */
public class EBRetryStrategyBackoff implements BackOff, EBRetryStrategy {
    public static final String NAME = "backoff";
    /**
     * The default initial interval value in milliseconds (0.5 seconds).
     */
    public static final int DEFAULT_INITIAL_INTERVAL_MILLIS = 500;
    /**
     * The default randomization factor (0.5 which results in a random period ranging between 50%
     * below and 50% above the retry interval).
     */
    public static final double DEFAULT_RANDOMIZATION_FACTOR = 0.5;
    /**
     * The default multiplier value (1.5 which is 50% increase per back off).
     */
    public static final double DEFAULT_MULTIPLIER = 1.5;
    /**
     * The default maximum back off time in milliseconds (1 minute).
     */
    public static final int DEFAULT_MAX_INTERVAL_MILLIS = 60000;
    /**
     * The default maximum elapsed time in milliseconds (15 minutes).
     */
    public static final int DEFAULT_MAX_ELAPSED_TIME_MILLIS = 900000;
    /**
     * The default maximum number of attempts.
     */
    public static final int DEFAULT_MAX_ATTEMPTS = -1;
    protected static final String FIELD_INITIAL_INTERVAL_MILLIS = "initialMillis";
    protected static final String FIELD_RANDOMIZATION_FACTOR = "randFact";
    protected static final String FIELD_MULTIPLIER = "mult";
    protected static final String FIELD_MAX_INTERVAL_MILLIS = "maxIntMillis";
    protected static final String FIELD_MAX_ELAPSED_TIME_MILLIS = "maxElapsedMillis";
    protected static final String FIELD_MAX_ATTEMPTS = "maxAttempts";
    /**
     * The initial retry interval in milliseconds.
     */
    private final int initialIntervalMillis;
    /**
     * The randomization factor to use for creating a range around the retry interval.
     * <p>
     * <p>
     * A randomization factor of 0.5 results in a random period ranging between 50% below and 50%
     * above the retry interval.
     * </p>
     */
    private final double randomizationFactor;
    /**
     * The value to multiply the current interval with for each retry attempt.
     */
    private final double multiplier;
    /**
     * The maximum value of the back off period in milliseconds. Once the retry interval reaches this
     * value it stops increasing.
     */
    private final int maxIntervalMillis;
    /**
     * The maximum value of attempts before failing.
     */
    private final int maxAttempts;
    /**
     * The maximum elapsed time after instantiating {@link EBRetryStrategyBackoff} or calling
     * {@link #reset()} after which {@link #nextBackOffMillis()} returns {@link BackOff#STOP}.
     */
    private final int maxElapsedTimeMillis;
    /**
     * The current retry interval in milliseconds.
     */
    private int currentIntervalMillis;
    /**
     * The current number of attempts
     */
    private int currentAttempts = 0;
    /**
     * The system time in nanoseconds. It is calculated when an ExponentialBackOffPolicy instance is
     * created and is reset when {@link #reset()} is called.
     */
    private long startTimeNanos;

    /**
     * Creates an instance of ExponentialBackOffPolicy using default values.
     * <p>
     * <p>
     * To override the defaults use {@link Builder}.
     * </p>
     * <p>
     * <ul>
     * <li>{@code initialIntervalMillis} defaults to {@link #DEFAULT_INITIAL_INTERVAL_MILLIS}</li>
     * <li>{@code randomizationFactor} defaults to {@link #DEFAULT_RANDOMIZATION_FACTOR}</li>
     * <li>{@code multiplier} defaults to {@link #DEFAULT_MULTIPLIER}</li>
     * <li>{@code maxIntervalMillis} defaults to {@link #DEFAULT_MAX_INTERVAL_MILLIS}</li>
     * <li>{@code maxElapsedTimeMillis} defaults in {@link #DEFAULT_MAX_ELAPSED_TIME_MILLIS}</li>
     * <li>{@code maxAttempts} defaults in {@link #DEFAULT_MAX_ATTEMPTS}</li>
     * </ul>
     */
    public EBRetryStrategyBackoff() {
        this(new Builder());
    }

    public EBRetryStrategyBackoff(JSONObject json) {
        this(new Builder().setJSON(json));
    }

    /**
     * @param builder builder
     */
    protected EBRetryStrategyBackoff(Builder builder) {
        initialIntervalMillis = builder.initialIntervalMillis;
        randomizationFactor = builder.randomizationFactor;
        multiplier = builder.multiplier;
        maxIntervalMillis = builder.maxIntervalMillis;
        maxElapsedTimeMillis = builder.maxElapsedTimeMillis;
        maxAttempts = builder.maxAttempts;
        if (initialIntervalMillis <= 0
                || (0 > randomizationFactor || randomizationFactor >= 1)
                || multiplier < 1
                || maxIntervalMillis < initialIntervalMillis
                || maxElapsedTimeMillis <= 0) {
            throw new IllegalArgumentException("Invalid input arguments");
        }
        reset();
    }

    /**
     * Returns a random value from the interval [randomizationFactor * currentInterval,
     * randomizationFactor * currentInterval].
     *
     * @param randomizationFactor   double
     * @param random                double
     * @param currentIntervalMillis int
     * @return
     */
    static int getRandomValueFromInterval(
            double randomizationFactor, double random, int currentIntervalMillis) {
        double delta = randomizationFactor * currentIntervalMillis;
        double minInterval = currentIntervalMillis - delta;
        double maxInterval = currentIntervalMillis + delta;
        // Get a random value from the range [minInterval, maxInterval].
        // The formula used below has a +1 because if the minInterval is 1 and the maxInterval is 3 then
        // we want a 33% chance for selecting either 1, 2 or 3.
        int randomValue = (int) (minInterval + (random * (maxInterval - minInterval + 1)));
        return randomValue;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <p>
     * This method calculates the next back off interval using the formula: randomized_interval =
     * retry_interval +/- (randomization_factor * retry_interval)
     * </p>
     * <p>
     * <p>
     * Subclasses may override if a different algorithm is required.
     * </p>
     *
     * @return long
     * @throws IOException IO exceptions only (beyond software control)
     */
    public long nextBackOffMillis() throws IOException {
        return nextBackOffMillis(true);
    }

    public long nextBackOffMillis(boolean inc) throws IOException {
        // Make sure we have not gone over the maximum elapsed time.
        if (getElapsedTimeMillis() > maxElapsedTimeMillis) {
            return STOP;
        }
        int randomizedInterval =
                getRandomValueFromInterval(randomizationFactor, Math.random(), currentIntervalMillis);

        if (inc) {
            incrementCurrentInterval();
        }

        return randomizedInterval;
    }

    /**
     * Returns the initial retry interval in milliseconds.
     *
     * @return int
     */
    public final int getInitialIntervalMillis() {
        return initialIntervalMillis;
    }

    /**
     * Returns the randomization factor to use for creating a range around the retry interval.
     * <p>
     * <p>
     * A randomization factor of 0.5 results in a random period ranging between 50% below and 50%
     * above the retry interval.
     * </p>
     *
     * @return double
     */
    public final double getRandomizationFactor() {
        return randomizationFactor;
    }

    /**
     * Returns the current retry interval in milliseconds.
     *
     * @return int
     */
    public final int getCurrentIntervalMillis() {
        return currentIntervalMillis;
    }

    /**
     * Returns the value to multiply the current interval with for each retry attempt.
     *
     * @return double
     */
    public final double getMultiplier() {
        return multiplier;
    }

    /**
     * Returns the maximum value of the back off period in milliseconds. Once the current interval
     * reaches this value it stops increasing.
     *
     * @return int
     */
    public final int getMaxIntervalMillis() {
        return maxIntervalMillis;
    }

    /**
     * Return the maximum number of tries that will be attempted for the EBRetryJob instance.
     *
     * @return int
     */
    public final int getMaxAttempts() {
        return maxAttempts;
    }

    /**
     * Returns the maximum elapsed time in milliseconds.
     * <p>
     * <p>
     * If the time elapsed since an {@link EBRetryStrategyBackoff} instance is created goes past the
     * max_elapsed_time then the method {@link #nextBackOffMillis()} starts returning
     * {@link BackOff#STOP}. The elapsed time can be reset by calling {@link #reset()}.
     * </p>
     *
     * @return int
     */
    public final int getMaxElapsedTimeMillis() {
        return maxElapsedTimeMillis;
    }

    /**
     * Returns the elapsed time in milliseconds since an {@link EBRetryStrategyBackoff} instance is
     * created and is reset when {@link #reset()} is called.
     * <p>
     * <p>
     * The elapsed time is computed using {@link System#nanoTime()}.
     * </p>
     *
     * @return long
     */
    public final long getElapsedTimeMillis() {
        return (System.nanoTime() - startTimeNanos) / 1000000;
    }

    /**
     * Increments the current interval by multiplying it with the multiplier.
     */
    private void incrementCurrentInterval() {
        // Check for overflow, if overflow is detected set the current interval to the max interval.
        if (currentIntervalMillis >= maxIntervalMillis / multiplier) {
            currentIntervalMillis = maxIntervalMillis;
        } else {
            currentIntervalMillis *= multiplier;
        }
    }

    @Override
    public String getName() {
        return "backoff";
    }

    @Override
    public void onFail() {
        currentAttempts += 1;
        incrementCurrentInterval();
    }

    @Override
    public void onSuccess() {

    }

    /**
     * Sets the interval back to the initial retry interval and restarts the timer.
     */
    public final void reset() {
        currentIntervalMillis = initialIntervalMillis;
        currentAttempts = 0;
        startTimeNanos = System.nanoTime();
    }

    @Override
    public boolean shouldContinue() {
        if (getElapsedTimeMillis() > maxElapsedTimeMillis) {
            return false;
        }

        if (maxAttempts >= 0 && currentAttempts >= maxAttempts) {
            return false;
        }

        return true;
    }

    @Override
    public long getWaitMilli() {
        try {
            return nextBackOffMillis(false);
        } catch (IOException e) {
            return STOP;
        }
    }

    @Override
    public EBRetryStrategy copy() {
        return new EBRetryStrategyBackoff.Builder()
                .setInitialIntervalMillis(getInitialIntervalMillis())
                .setMaxAttempts(getMaxAttempts())
                .setMaxElapsedTimeMillis(getMaxElapsedTimeMillis())
                .setMaxIntervalMillis(getMaxIntervalMillis())
                .setMultiplier(getMultiplier())
                .setRandomizationFactor(getRandomizationFactor())
                .build();
    }

    @Override
    public JSONObject toJSON(JSONObject json) {
        if (json == null) {
            json = new JSONObject();
        }

        if (getMaxAttempts() != DEFAULT_MAX_ATTEMPTS) {
            json.put(FIELD_MAX_ATTEMPTS, getMaxAttempts());
        }

        if (getMaxElapsedTimeMillis() != DEFAULT_MAX_ELAPSED_TIME_MILLIS) {
            json.put(FIELD_MAX_ELAPSED_TIME_MILLIS, getMaxElapsedTimeMillis());
        }

        if (getMaxIntervalMillis() != DEFAULT_MAX_INTERVAL_MILLIS) {
            json.put(FIELD_MAX_INTERVAL_MILLIS, getMaxIntervalMillis());
        }

        if (getMultiplier() != DEFAULT_MULTIPLIER) {
            json.put(FIELD_MULTIPLIER, getMultiplier());
        }

        if (getRandomizationFactor() != DEFAULT_RANDOMIZATION_FACTOR) {
            json.put(FIELD_RANDOMIZATION_FACTOR, getRandomizationFactor());
        }

        if (getInitialIntervalMillis() != DEFAULT_INITIAL_INTERVAL_MILLIS) {
            json.put(FIELD_INITIAL_INTERVAL_MILLIS, getInitialIntervalMillis());
        }

        return json;
    }

    /**
     * Builder for {@link EBRetryStrategyBackoff}.
     * <p>
     * <p>
     * Implementation is not thread-safe.
     * </p>
     */
    public static class Builder {

        /**
         * The initial retry interval in milliseconds.
         */
        int initialIntervalMillis = DEFAULT_INITIAL_INTERVAL_MILLIS;

        /**
         * The randomization factor to use for creating a range around the retry interval.
         * <p>
         * <p>
         * A randomization factor of 0.5 results in a random period ranging between 50% below and 50%
         * above the retry interval.
         * </p>
         */
        double randomizationFactor = DEFAULT_RANDOMIZATION_FACTOR;

        /**
         * The value to multiply the current interval with for each retry attempt.
         */
        double multiplier = DEFAULT_MULTIPLIER;

        /**
         * The maximum value of the back off period in milliseconds. Once the retry interval reaches
         * this value it stops increasing.
         */
        int maxIntervalMillis = DEFAULT_MAX_INTERVAL_MILLIS;

        /**
         * The maximum value of attempts before failing.
         */
        int maxAttempts = DEFAULT_MAX_ATTEMPTS;

        /**
         * The maximum elapsed time in milliseconds after instantiating {@link EBRetryStrategyBackoff} or
         * calling {@link #reset()} after which {@link #nextBackOffMillis()} returns
         * {@link BackOff#STOP}.
         */
        int maxElapsedTimeMillis = DEFAULT_MAX_ELAPSED_TIME_MILLIS;

        public Builder() {
        }

        /**
         * Builds a new instance of {@link EBRetryStrategyBackoff}.
         *
         * @return new instance of EBRetryStrategyBackoff
         */
        public EBRetryStrategyBackoff build() {
            return new EBRetryStrategyBackoff(this);
        }

        /**
         * Returns the initial retry interval in milliseconds. The default value is
         * {@link #DEFAULT_INITIAL_INTERVAL_MILLIS}.
         *
         * @return int
         */
        public final int getInitialIntervalMillis() {
            return initialIntervalMillis;
        }

        /**
         * Sets the initial retry interval in milliseconds. The default value is
         * {@link #DEFAULT_INITIAL_INTERVAL_MILLIS}. Must be {@code > 0}.
         * <p>
         * <p>
         * Overriding is only supported for the purpose of calling the super implementation and changing
         * the return type, but nothing else.
         * </p>
         *
         * @param initialIntervalMillis int
         * @return new instance of Builder
         */
        public Builder setInitialIntervalMillis(int initialIntervalMillis) {
            this.initialIntervalMillis = initialIntervalMillis;
            return this;
        }

        /**
         * Returns the randomization factor to use for creating a range around the retry interval. The
         * default value is {@link #DEFAULT_RANDOMIZATION_FACTOR}.
         * <p>
         * <p>
         * A randomization factor of 0.5 results in a random period ranging between 50% below and 50%
         * above the retry interval.
         * </p>
         * <p>
         * <p>
         * Overriding is only supported for the purpose of calling the super implementation and changing
         * the return type, but nothing else.
         * </p>
         *
         * @return double
         */
        public final double getRandomizationFactor() {
            return randomizationFactor;
        }

        /**
         * Sets the randomization factor to use for creating a range around the retry interval. The
         * default value is {@link #DEFAULT_RANDOMIZATION_FACTOR}. Must fall in the range
         * {@code 0 <= randomizationFactor < 1}.
         * <p>
         * <p>
         * A randomization factor of 0.5 results in a random period ranging between 50% below and 50%
         * above the retry interval.
         * </p>
         * <p>
         * <p>
         * Overriding is only supported for the purpose of calling the super implementation and changing
         * the return type, but nothing else.
         * </p>
         *
         * @param randomizationFactor double
         * @return new instance of Builder
         */
        public Builder setRandomizationFactor(double randomizationFactor) {
            this.randomizationFactor = randomizationFactor;
            return this;
        }

        /**
         * Returns the value to multiply the current interval with for each retry attempt. The default
         * value is {@link #DEFAULT_MULTIPLIER}.
         *
         * @return double
         */
        public final double getMultiplier() {
            return multiplier;
        }

        /**
         * Sets the value to multiply the current interval with for each retry attempt. The default
         * value is {@link #DEFAULT_MULTIPLIER}. Must be {@code >= 1}.
         * <p>
         * <p>
         * Overriding is only supported for the purpose of calling the super implementation and changing
         * the return type, but nothing else.
         * </p>
         *
         * @param multiplier double
         * @return new instance of Builder
         */
        public Builder setMultiplier(double multiplier) {
            this.multiplier = multiplier;
            return this;
        }

        /**
         * Returns the maximum value of the back off period in milliseconds. Once the current interval
         * reaches this value it stops increasing. The default value is
         * {@link #DEFAULT_MAX_INTERVAL_MILLIS}. Must be {@code >= initialInterval}.
         *
         * @return int
         */
        public final int getMaxIntervalMillis() {
            return maxIntervalMillis;
        }

        /**
         * Sets the maximum value of the back off period in milliseconds. Once the current interval
         * reaches this value it stops increasing. The default value is
         * {@link #DEFAULT_MAX_INTERVAL_MILLIS}.
         * <p>
         * <p>
         * Overriding is only supported for the purpose of calling the super implementation and changing
         * the return type, but nothing else.
         * </p>
         *
         * @param maxIntervalMillis int
         * @return new Builder instance
         */
        public Builder setMaxIntervalMillis(int maxIntervalMillis) {
            this.maxIntervalMillis = maxIntervalMillis;
            return this;
        }

        /**
         * Returns the maximum elapsed time in milliseconds. The default value is
         * {@link #DEFAULT_MAX_ELAPSED_TIME_MILLIS}.
         * <p>
         * <p>
         * If the time elapsed since an {@link EBRetryStrategyBackoff} instance is created goes past the
         * max_elapsed_time then the method {@link #nextBackOffMillis()} starts returning
         * {@link BackOff#STOP}. The elapsed time can be reset by calling {@link #reset()}.
         * </p>
         *
         * @return int
         */
        public final int getMaxElapsedTimeMillis() {
            return maxElapsedTimeMillis;
        }

        /**
         * Sets the maximum elapsed time in milliseconds. The default value is
         * {@link #DEFAULT_MAX_ELAPSED_TIME_MILLIS}. Must be {@code > 0}.
         * <p>
         * <p>
         * If the time elapsed since an {@link EBRetryStrategyBackoff} instance is created goes past the
         * max_elapsed_time then the method {@link #nextBackOffMillis()} starts returning
         * {@link BackOff#STOP}. The elapsed time can be reset by calling {@link #reset()}.
         * </p>
         * <p>
         * <p>
         * Overriding is only supported for the purpose of calling the super implementation and changing
         * the return type, but nothing else.
         * </p>
         *
         * @param maxElapsedTimeMillis int
         * @return new Builder instance
         */
        public Builder setMaxElapsedTimeMillis(int maxElapsedTimeMillis) {
            this.maxElapsedTimeMillis = maxElapsedTimeMillis;
            return this;
        }

        /**
         * Maximum number of attempts
         *
         * @return number of attempts
         */
        public int getMaxAttempts() {
            return this.maxAttempts;
        }

        /**
         * Sets maximum number of attempts before quitting.
         * The default value is {@link #DEFAULT_MAX_ATTEMPTS}. If is {@code < 0} this limit is not taken into account.
         *
         * @param maxAttempts number of attempts
         * @return this
         */
        public Builder setMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        /**
         * Reads serialized settings from the JSON
         *
         * @param json json to use
         * @return this
         */
        public Builder setJSON(JSONObject json) {
            if (json.has(FIELD_MAX_ATTEMPTS)) {
                maxAttempts = EBUtils.getAsInteger(json, FIELD_MAX_ATTEMPTS, 10);
            }
            if (json.has(FIELD_MAX_ELAPSED_TIME_MILLIS)) {
                maxElapsedTimeMillis = EBUtils.getAsInteger(json, FIELD_MAX_ELAPSED_TIME_MILLIS, 10);
            }
            if (json.has(FIELD_MAX_INTERVAL_MILLIS)) {
                maxIntervalMillis = EBUtils.getAsInteger(json, FIELD_MAX_INTERVAL_MILLIS, 10);
            }
            if (json.has(FIELD_INITIAL_INTERVAL_MILLIS)) {
                initialIntervalMillis = EBUtils.getAsInteger(json, FIELD_INITIAL_INTERVAL_MILLIS, 10);
            }
            if (json.has(FIELD_RANDOMIZATION_FACTOR)) {
                randomizationFactor = EBUtils.getAsDouble(json, FIELD_RANDOMIZATION_FACTOR);
            }
            if (json.has(FIELD_MULTIPLIER)) {
                multiplier = EBUtils.getAsDouble(json, FIELD_MULTIPLIER);
            }

            return this;
        }
    }
}

