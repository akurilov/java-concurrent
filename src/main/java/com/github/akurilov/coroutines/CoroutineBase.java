package com.github.akurilov.coroutines;

import java.io.IOException;

/**
 * The base class for all coroutines.
 */
public abstract class CoroutineBase
extends StoppableTaskBase
implements Coroutine {

	private final CoroutinesProcessor coroutinesProcessor;

	private volatile boolean stoppedFlag = false;

	protected CoroutineBase(final CoroutinesProcessor coroutinesProcessor) {
		this.coroutinesProcessor = coroutinesProcessor;
	}

	@Override
	public final void start() {
		coroutinesProcessor.start(this);
	}

	/**
	 * Decorates the invocation method with timing.
	 */
	@Override
	protected final void invoke() {
		long t = System.nanoTime();
		invokeTimed(t);
	}

	/**
	 * The method implementation should use the start time to check its own duration in order to not to exceed the
	 * invocation time limit (250ms)
	 * @param startTimeNanos the time when the invocation started
	 */
	protected abstract void invokeTimed(final long startTimeNanos);

	/**
	 * Soft stop. Prevent the task for further invocations.
	 * Current invocation (if executing) remains active until its end.
	 */
	@Override
	public final void stop() {
		coroutinesProcessor.stop(this);
		stoppedFlag = true;
	}

	@Override
	public final boolean isStopped() {
		return stoppedFlag;
	}

	@Override
	public final void close()
	throws IOException {
		super.close();
	}
}
