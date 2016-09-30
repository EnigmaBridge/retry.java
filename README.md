# Retry mechanism

[![Build Status][1]][2]

Very simple retry mechanism with small dependency footprint. Visit [http://enigmabridge.github.io/retry.java](http://enigmabridge.github.io/retry.java) for Javadoc documentation.

## Maven repository

```xml
<dependency>
  <groupId>com.enigmabridge</groupId>
  <artifactId>retry</artifactId>
  <version>0.1.0</version>
</dependency>
```

## Gradle

```gradle
compile 'com.enigmabridge:retry:0.1.0'
```

## Contributing

Pull requests and bug reports are welcome

[1]: https://travis-ci.org/EnigmaBridge/retry.java.svg
[2]: https://travis-ci.org/EnigmaBridge/retry.java

## Examples 

#### Synchronous Backoff
```java
// Initialize retry strategy to Backoff
final EBRetryStrategy retryStrategy = new EBRetryStrategyBackoff.Builder()
                                        .setMaxElapsedTimeMillis(1000*60*5) // Limit on total time
                                        .build();

// New retry mechanism instance, for each job.
final EBRetry<ResultObject, Throwable> ebRetry = new EBRetry<ResultObject, Throwable>(retryStrategy.copy());

// Define retry job
ebRetry.setJob(new EBRetryJobSimpleSafeThrErr<ResultObject>() {
    @Override
    public void runAsyncNoException(EBCallback<ResultObject, Throwable> callback) throws Throwable {
        try {
            // Do the actuall job here. May be called multiple times.
            final ResultObject result = our_task_to_retry();
            
            // Signalize success to the retry object so it knows we can quit trying.
            callback.onSuccess(result);

        } catch(IOException exception) {
            // You mau log it if you want
            //LOG.debug("Job failed");
            
            // Call fail callback so retry mechanism knows this attempt was
            // not successful and it should continue trying or give up
            // if number of attempts or total ellapsed time exceeded threshold.
            //
            // Fail accepts job error object. We usually use Throwable as an job error
            // as it usually corresponds with the failure (e.g., IOException on network error).
            // We wrap throwable to the job error and pass it to the fail callback.
            callback.onFail(new EBRetryJobErrorThr(exception), false);
        }
    }
});

// Start the job synchronously.
try {
    return ebRetry.runSync();

} catch (EBRetryFailedException e) {
    throw new IOException(e);

} catch (EBRetryException e){
    throw new IOException("Fatal request error", e);
}
```

### Asynchronous backoff
Retry mechanism supports asynchronous jobs.

```java
// Initialize retry strategy to Backoff
final EBRetryStrategy retryStrategy = new EBRetryStrategyBackoff.Builder()
                                        .setMaxElapsedTimeMillis(1000*60*5) // Limit on total time
                                        .build();

// New retry mechanism instance, for each job.
final EBRetry<ResultObject, Throwable> ebRetry = new EBRetry<ResultObject, Throwable>(retryStrategy.copy());

// Define retry job
ebRetry.setJob(new EBRetryJobSimpleSafeThrErr<ResultObject>() {
    @Override
    public void runAsyncNoException(EBCallback<ResultObject, Throwable> callback) throws Throwable {
        try {
            // Do the actuall job here. May be called multiple times.
            final ResultObject result = our_task_to_retry();
            
            // Signalize success to the retry object so it knows we can quit trying.
            callback.onSuccess(result);

        } catch(IOException exception) {
            callback.onFail(new EBRetryJobErrorThr(exception), false);
        }
    }
});

// Define async listener. Will be called on success / fail.
// Fail = backoff strategy expired (threshold exceeded) 
ebRetry.addListener(new EBRetryListener<EBRawResponse, Throwable>() {
    @Override
    public void onSuccess(EBRawResponse ebRawResponse, EBRetry<EBRawResponse, Throwable> retry) {
        // hooray, success
    }

    @Override
    public void onFail(EBRetryJobError<Throwable> error, EBRetry<EBRawResponse, Throwable> retry) {
        // well, we tried
    }
});

// Run asynchronously. 
// Keep future if you want to detect if job is still running
// Or to cancel it or to skip current backoff waiting interval (runNow())
final EBFuture<EBRawResponse, Throwable> future = ebRetry.runAsync();
```

### Options to build [EBRetryStrategyBackoff.Builder](https://enigmabridge.github.io/retry.java/com/enigmabridge/retry/EBRetryStrategyBackoff.Builder.html) - quick reference

 - `setMaxAttempts(int tries)` - how many times will be the EBRetryJob executed;
 - `setMaxIntervalMillis(int milliseconds)` - maximum time for the job to be run - it will be interrupted when the time limit is reached;
 - `setMaxElapsedTimeMillis(int milliseconds)` - the maximum value of a counter, when reached, nextBackoffMillis() will start returning BackOff.STOP;
 - `setMultiplier(int multiplier)` - value to multiply the current interval with for each retry attempt;
 - `setRandomizationFactor(double randomizationFactor)` - a factor of 0.5 results in a random period ranging between 50% below and 50% above the retry interval;
 - `setJSON(org.json.JSONObject object)` - reads settings from JSON;
 - `setInitialIntervalLimit(int initialIntervalLimit)` -  initial retry interval in milliseconds.


