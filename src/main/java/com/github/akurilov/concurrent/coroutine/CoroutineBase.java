package com.github.akurilov.concurrent.coroutine;

import com.github.akurilov.concurrent.AsyncRunnableBase;

/**
 * The base class for all coroutines.
 */
public abstract class CoroutineBase
extends AsyncRunnableBase
implements Coroutine {

	private final CoroutinesExecutor executor;

	protected CoroutineBase(final CoroutinesExecutor executor) {
		this.executor = executor;
	}

	@Override
	protected void doStart() {
		executor.start(this);
	}

	/**
	 * Decorates the invocation method with timing.
	 */
	@Override
	public final void invoke() {
		invokeTimed(System.nanoTime());
	}

	/**
	 * The method implementation should use the start time to check its own duration in order to not
	 * to exceed the invocation time limit (100 ms)
	 * @param startTimeNanos the time when the invocation started
	 */
	protected abstract void invokeTimed(final long startTimeNanos);

	@Override
	protected void doStop() {
		executor.stop(this);
	}
}
