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
		if(!writeLock.tryLock()) {
			throw new IllegalStateException();
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
