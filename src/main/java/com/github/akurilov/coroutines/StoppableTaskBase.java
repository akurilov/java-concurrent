package com.github.akurilov.coroutines;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Logger;

/**
 The base for a stoppable task
 */
public abstract class StoppableTaskBase
implements StoppableTask {

	private static final Logger LOG = Logger.getLogger(StoppableTaskBase.class.getName());

	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock();
	protected final Lock readLock = readWriteLock.readLock();
	protected final Lock writeLock = readWriteLock.writeLock();

	@Override
	public void run() {
		if(readLock.tryLock()) {
			try {
				invoke();
			} finally {
				readLock.unlock();
			}
		}
	}

	@Override
	public abstract void stop();

	@Override
	public abstract boolean isStopped();

	@Override
	public void close()
	throws IOException {
		try {
			if(!writeLock.tryLock(CLOSE_TIMEOUT_NANOS, TimeUnit.NANOSECONDS)) {
				LOG.warning("Close lock timeout");
			}
		} catch(final InterruptedException e) {
			LOG.severe("Waiting the close lock was interrupted: " + e.getMessage());
		} finally {
			// clean up anyway
			doClose();
		}
	}

	@Override
	public final boolean isClosed() {
		final boolean writeLocked = writeLock.tryLock();
		if(writeLocked) {
			writeLock.unlock();
		}
		return !writeLocked;
	}

	/**
	 * The task invocation method. Will not run if the task is stopped (closed).
	 */
	protected abstract void invoke();

	/**
	 * Implement this method for the cleanup purposes.
	 * @throws IOException
	 */
	protected abstract void doClose()
	throws IOException;
}
