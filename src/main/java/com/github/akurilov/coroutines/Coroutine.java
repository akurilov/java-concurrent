package com.github.akurilov.coroutines;

/**
 * Base interface for all coroutines
 */
public interface Coroutine
extends StoppableTask {

	/**
	 The soft limit for the coroutine invocation duration.
	 The coroutine implementation should care about its own invocation duration.
	 */
	int TIMEOUT_NANOS = 100_000_000;

	/**
	 Starts the coroutine execution by adding self to the registry.
	 Note that it's not safe to start the coroutine multiple times.
	 */
	void start();
}
