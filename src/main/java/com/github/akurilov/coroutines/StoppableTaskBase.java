package com.github.akurilov.coroutines;

import java.io.IOException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 The base for a stoppable task
 */
public abstract class StoppableTaskBase
implements StoppableTask {

	private final ReadWriteLock readWriteLock = new ReentrantReadWriteLock(true);
	protected final Lock readLock = readWriteLock.readLock();
	protected final Lock writeLock = readWriteLock.writeLock();

	private volatile boolean stoppedFlag = false;

	@Override
	public void run() {
		if(!stoppedFlag && readLock.tryLock()) {
			try {
				invoke();
			} finally {
				readLock.unlock();
			}
		}
	}

	/**
	 * Soft stop. Prevent the task for further invocations.
	 * Current invocation (if executing) remains active until its end.
	 */
	@Override
	public final void stop() {
		stoppedFlag = true;
		doStop();
	}

	protected abstract void doStop();

	@Override
	public final boolean isStopped() {
		return stoppedFlag;
	}

	/**
	 * Thread-safe close method. Fails if the task is running/closed throwing an exception.
	 * @throws IOException
	 * @throws IllegalStateException if the task was locked by run() method either closed before
	 */
	@Override
	public void close()
	throws IOException {
		stop();
		if(!writeLock.tryLock()) {
			throw new IllegalStateException("Task is locked");
		}
	}

	@Override
	public final boolean isClosed() {
		final boolean writeWasLocked = !writeLock.tryLock();
		if(!writeWasLocked) {
			writeLock.unlock();
		}
		return stoppedFlag && writeWasLocked;
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
