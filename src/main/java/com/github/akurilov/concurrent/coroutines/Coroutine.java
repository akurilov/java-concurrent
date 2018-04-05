package com.github.akurilov.concurrent.coroutines;

import com.github.akurilov.concurrent.AsyncRunnable;

/**
 * Base interface for all coroutines
 */
public interface Coroutine
extends AsyncRunnable {

	/**
	 The soft limit for the coroutine invocation duration.
	 The coroutine implementation should care about its own invocation duration.
	 */
	int TIMEOUT_NANOS = 100_000_000;

	/**
	 * Perform the work
	 */
	void invoke();
}
