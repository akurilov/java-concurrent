package com.github.akurilov.concurrent.coroutines;

import com.github.akurilov.concurrent.AsyncRunnableBase;

import java.util.logging.Logger;

/**
 * The base class for all coroutines.
 */
public abstract class CoroutineBase
extends AsyncRunnableBase
implements Coroutine {

	private static final Logger LOG = Logger.getLogger(CoroutineBase.class.getName());

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
