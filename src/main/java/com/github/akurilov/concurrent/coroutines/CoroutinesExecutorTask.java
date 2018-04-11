package com.github.akurilov.concurrent.coroutines;

import com.github.akurilov.concurrent.AsyncRunnableBase;

import java.util.Queue;
import java.util.concurrent.locks.LockSupport;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class CoroutinesExecutorTask
extends AsyncRunnableBase
implements Runnable {

	private final static Logger LOG = Logger.getLogger(CoroutinesExecutorTask.class.getName());

	private final Queue<Coroutine> coroutines;
	private final boolean backgroundFlag;

	public CoroutinesExecutorTask(
		final Queue<Coroutine> coroutines, final boolean backgroundFlag
	) {
		this.coroutines = coroutines;
		this.backgroundFlag = backgroundFlag;
	}

	@Override
	public final void run() {
		while(isStarted()) {
			if(coroutines.size() == 0) {
				try {
					Thread.sleep(1);
				} catch(final InterruptedException e) {
					break;
				}
			} else {
				for(final var nextCoroutine : coroutines) {
					try {
						if(nextCoroutine.isStarted()) {
							nextCoroutine.invoke();
						}
					} catch(final Throwable t) {
						LOG.log(
							Level.WARNING, "Coroutine \"" + nextCoroutine + "\" failed",
							t
						);
					}
					if(backgroundFlag) {
						LockSupport.parkNanos(1);
					}
				}
			}
		}
	}
}
