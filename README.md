# Introduction

The library supporting the alternative concurrency model
(coroutine-like). Introduces the so called reentrant tasks which are
useful to execute a lot of periodic/long tasks using a fixed count of
the threads.

In this library, the coroutine is such stoppable task which executes
for some very short time avoiding any blocks multiple times
([in the reentrant way](https://en.wikipedia.org/wiki/Microthread)).
A basic coroutine instance may be executing by several different threads
at any moment of time so it should have thread-safe user code. To
implement the protected, thread-safe coroutine the special
`ExclusiveCoroutineBase` class is provided. An exclusive coroutine is
being executed by only one thread at any moment of time.

The coroutines are executed by coroutine processors. Any coroutine
processor has the thread-safe registry. The coroutine processor threads
iterate the coroutines registry and invoke the coroutines sequentially.
As far as coroutine processor is multithreaded the coroutines are being
executed concurrently also.

# Usage

## Gradle

```groovy
compile group: 'com.github.akurilov', name: 'java-concurrent', version: '2.0.3'
```

## Implementing Basic Coroutine

To implement the simpliest coroutine one should extend the
`CoroutineBase` class:

```java
package com.github.akurilov.concurrent.coroutines.example;

import com.github.akurilov.concurrent.coroutines.CoroutineBase;
import com.github.akurilov.concurrent.coroutines.CoroutineProcessor;

public class HelloWorldCoroutine
extends CoroutineBase {

    public HelloWorldCoroutine(final CoroutinesProcessor coroutinesProcessor) {
        super(coroutinesProcessor);
    }

    @Override
    protected void invokeTimed(final long startTimeNanos) {
        System.out.println("Hello world");
    }

    @Override
    protected void doClose()
    throws IOException {

    }
}
```

The method `invokeTimed` does the useful work. The example code below
utilizes that coroutine:

```java
package com.github.akurilov.concurrent.coroutines.example;

import com.github.akurilov.concurrent.coroutines.Coroutine;
import com.github.akurilov.concurrent.coroutines.CoroutinesProcessor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class Main {

    public static void main(final String... args)
    throws InterruptedException, IOException {

        final CoroutinesProcessor coroutinesProcessor = new CoroutinesProcessor();
        final Coroutine helloCoroutine = new HelloWorldCoroutine(coroutinesProcessor);
        helloCoroutine.start();
        TimeUnit.SECONDS.sleep(10);
        helloCoroutine.close();
    }
}
```

### invokeTimed notes

The code executed in the `invokeTimed` method should follow the rules:
* Do not block if possible
* Take care of own thread safety
* Do not exceed the timeout (`Coroutine.TIMEOUT_NANOS`)

The invoked code should take the responsibility on the time of its
execution. Example:

```java
    @Override
    protected void invokeTimed(long startTimeNanos) {
        for(int i = workBegin; i < workEnd; i ++) {
            doSomeUsefulWork(i);
            // yes, I know that the statement below may invoke Satan
            // but for simplicity it doesn't expect the negative result
            if(System.nanoTime() - startTimeNanos > TIMEOUT_NANOS) {
                break;
            }
        }
    }
```

## Implementing Exclusive Coroutine

An exclusive coroutine is restricted by a single thread. It allows:
* Consume less CPU resources (useful for "background" tasks)
* Don't care of thread safety

```java
package com.github.akurilov.concurrent.coroutines.example;

...
import com.github.akurilov.concurrent.coroutines.ExclusiveCoroutineBase;

public class HelloWorldExclusiveCoroutine
extends ExclusiveCoroutineBase {

    ...

    @Override
    protected void invokeTimedExclusively(long startTimeNanos) {
        System.out.println("Hello world!");
    }
    ...
```

## Other Coroutine Implementations

There are some other coroutine implementations included into the library
 for the user reference. These coroutines are used in the
[Mongoose](https://github.com/emc-mongoose/mongoose-base) project widely
and proved the coroutines approach efficiency.
