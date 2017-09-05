package com.github.akurilov.coroutines;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * The base class for all coroutines.
 */
public abstract class CoroutineBase
extends StoppableTaskBase
implements Coroutine {

	private static final Logger LOG = Logger.getLogger(CoroutineBase.class.getName());

	private final CoroutinesProcessor coroutinesProcessor;

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

	@Override
	protected void doStop() {
		coroutinesProcessor.stop(this);
	}

	/**
	 * Attempts to wait before the actual closing if locked.
	 * @throws IOException
	 */
	@Override
	public final void close()
	throws IOException {
		stop();
		try {
			if(!writeLock.tryLock(TIMEOUT_NANOS, TimeUnit.NANOSECONDS)) {
				LOG.warning("Coroutine close timeout");
			}
		} catch(final InterruptedException e) {
			LOG.severe("Coroutine close interrupted");
		} finally {
			doClose();
		}
	}
}
