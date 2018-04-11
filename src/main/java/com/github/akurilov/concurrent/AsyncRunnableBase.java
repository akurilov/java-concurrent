package com.github.akurilov.concurrent;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static com.github.akurilov.concurrent.AsyncRunnable.State.FINISHED;
import static com.github.akurilov.concurrent.AsyncRunnable.State.INITIAL;
import static com.github.akurilov.concurrent.AsyncRunnable.State.SHUTDOWN;
import static com.github.akurilov.concurrent.AsyncRunnable.State.STARTED;
import static com.github.akurilov.concurrent.AsyncRunnable.State.STOPPED;

public abstract class AsyncRunnableBase
implements AsyncRunnable {

	private final AtomicReference<State> stateRef = new AtomicReference<>(INITIAL);
	protected final Object state = new Object();

	@Override
	public final State state() {
		return stateRef.get();
	}

	@Override
	public boolean isInitial() {
		return INITIAL.equals(stateRef.get());
	}

	@Override
	public boolean isStarted() {
		return STARTED.equals(stateRef.get());
	}

	@Override
	public boolean isShutdown() {
		return SHUTDOWN.equals(stateRef.get());
	}

	@Override
	public boolean isStopped() {
		return STOPPED.equals(stateRef.get());
	}

	@Override
	public boolean isFinished() {
		return FINISHED.equals(stateRef.get());
	}

	@Override
	public boolean isClosed() {
		return null == stateRef.get();
	}

	@Override
	public final AsyncRunnableBase start()
	throws IllegalStateException {
		if(stateRef.compareAndSet(INITIAL, STARTED) || stateRef.compareAndSet(STOPPED, STARTED)) {
			synchronized(state) {
				doStart();
				state.notifyAll();
			}
		} else {
			throw new IllegalStateException(
				"Not allowed to start while state is \"" + stateRef.get() + "\""
			);
		}
		return this;
	}

	@Override
	public final AsyncRunnableBase shutdown()
	throws IllegalStateException {
		if(stateRef.compareAndSet(STARTED, SHUTDOWN)) {
			synchronized(state) {
				doShutdown();
				state.notifyAll();
			}
		} else {
			throw new IllegalStateException(
				"Not allowed to shutdown while state is \"" + stateRef.get() + "\""
			);
		}
		return this;
	}

	@Override
	public final AsyncRunnableBase stop()
	throws IllegalStateException, RemoteException {
		try {
			shutdown();
		} catch(final IllegalStateException ignored) {
		}
		if(stateRef.compareAndSet(STARTED, STOPPED) || stateRef.compareAndSet(SHUTDOWN, STOPPED)) {
			synchronized(state) {
				doStop();
				state.notifyAll();
			}
		} else {
			throw new IllegalStateException(
				"Not allowed to stop while state is \"" + stateRef.get() + "\""
			);
		}
		return this;
	}

	@Override
	public final AsyncRunnableBase await()
	throws IllegalStateException, InterruptedException {
		await(Long.MAX_VALUE, TimeUnit.DAYS);
		return this;
	}

	@Override
	public boolean await(final long timeout, final TimeUnit timeUnit)
	throws IllegalStateException, InterruptedException {
		final var timeOutMilliSec = timeUnit.toMillis(timeout);
		final var t = System.currentTimeMillis();
		while(isStarted() || isShutdown()) {
			if(System.currentTimeMillis() - t >= timeOutMilliSec) {
				return false;
			}
			synchronized(state) {
				state.wait(100);
			}
		}
		return true;
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
		synchronized(state) {
			if(null != stateRef.get()) {
				doClose();
				stateRef.set(null);
				state.notifyAll();
			}
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
