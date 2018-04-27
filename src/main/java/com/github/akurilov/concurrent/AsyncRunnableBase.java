package com.github.akurilov.concurrent;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import static com.github.akurilov.concurrent.AsyncRunnable.State.FINISHED;
import static com.github.akurilov.concurrent.AsyncRunnable.State.INITIAL;
import static com.github.akurilov.concurrent.AsyncRunnable.State.SHUTDOWN;
import static com.github.akurilov.concurrent.AsyncRunnable.State.STARTED;
import static com.github.akurilov.concurrent.AsyncRunnable.State.STOPPED;

public abstract class AsyncRunnableBase
implements AsyncRunnable {

	private volatile State state = State.INITIAL;

	private final Lock stateLock = new ReentrantLock();
	private final Condition stateChanged = stateLock.newCondition();

	@Override
	public final State state() {
		return state;
	}

	@Override
	public boolean isInitial() {
		return INITIAL == state;
	}

	@Override
	public boolean isStarted() {
		return STARTED == state;
	}

	@Override
	public boolean isShutdown() {
		return SHUTDOWN == state;
	}

	@Override
	public boolean isStopped() {
		return STOPPED == state;
	}

	@Override
	public boolean isFinished() {
		return FINISHED == state;
	}

	@Override
	public boolean isClosed() {
		return null == state;
	}

	@Override
	public final AsyncRunnableBase start()
	throws IllegalStateException {
		stateLock.lock();
		try {
			if(state == INITIAL || state == STOPPED) {
				doStart();
				state = STARTED;
				stateChanged.signalAll();
			} else {
				throw new IllegalStateException(
					"Not allowed to start while state is \"" + state + "\""
				);
			}
		} finally {
			stateLock.unlock();
		}
		return this;
	}

	@Override
	public final AsyncRunnableBase shutdown()
	throws IllegalStateException {
		stateLock.lock();
		try {
			if(state == STARTED) {
				doShutdown();
				state = SHUTDOWN;
				stateChanged.signalAll();
			} else {
				throw new IllegalStateException(
					"Not allowed to shutdown while state is \"" + state + "\""
				);
			}
		} finally {
			stateLock.unlock();
		}
		return this;
	}

	@Override
	public final AsyncRunnableBase stop()
	throws IllegalStateException {
		stateLock.lock();
		try {
			if(state == STARTED || state == SHUTDOWN) {
				doStop();
				state = STOPPED;
				stateChanged.signalAll();
			} else {
				throw new IllegalStateException(
					"Not allowed to stop while state is \"" + state + "\""
				);
			}
		} finally {
			stateLock.unlock();
		}
		return this;
	}

	@Override
	public final AsyncRunnableBase await()
	throws IllegalStateException, InterruptedException {
		await(Long.MAX_VALUE, TimeUnit.MILLISECONDS);
		return this;
	}

	@Override
	public boolean await(final long timeout, final TimeUnit timeUnit)
	throws IllegalStateException, InterruptedException {
		final long invokeTimeMillis = System.currentTimeMillis();
		final long timeOutMillis = timeUnit.toMillis(timeout);
		long elapsedTimeMillis;
		while(timeOutMillis > (elapsedTimeMillis = System.currentTimeMillis() - invokeTimeMillis)) {
			if(state != STARTED && state != SHUTDOWN) {
				return true; // condition is reached
			} else {
				if(stateLock.tryLock(timeOutMillis - elapsedTimeMillis, TimeUnit.MILLISECONDS)) {
					try {
						// spent a time to wait for the state lock, need to update the elapsed time
						elapsedTimeMillis = System.currentTimeMillis() - invokeTimeMillis;
						// recheck for the timeout condition
						if(timeOutMillis > elapsedTimeMillis) {
							if(
								stateChanged.await(
									timeOutMillis - elapsedTimeMillis, TimeUnit.MILLISECONDS
								)
							) { // the state is changed, recheck the condition
								if(state != STARTED && state != SHUTDOWN) {
									return true;
								} // continue otherwise (no timeout yet, condition is not reached)
							}
						} else { // timeout, exit the loop
							break;
						}
					} finally {
						stateLock.unlock();
					}
				}
			}
		}
		return state != STARTED && state != SHUTDOWN;
	}

	@Override
	public void close()
	throws IllegalStateException, IOException {
		// stop first
		try {
			stop();
		} catch(final IllegalStateException ignored) {
		}
		// then close actually
		stateLock.lock();
		try {
			if(null != state) {
				doClose();
				state = null;
				stateChanged.signalAll();
			}
		} finally {
			stateLock.unlock();
		}
	}

	protected void doStart() {
	}

	protected void doShutdown() {
	}

	protected void doStop() {
	}

	protected void doClose()
	throws IOException {
	}
}
