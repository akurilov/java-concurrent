package com.github.akurilov.concurrent;

import java.util.Map;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by andrey on 23.08.17.
 */
public class ContextAwareThreadFactory
implements ThreadFactory {

	protected static final Thread.UncaughtExceptionHandler exceptionHandler = (t, e) -> {
		synchronized(System.err) {
			System.err.println("Uncaught exception in the thread \"" + t.getName() + "\":");
			e.printStackTrace(System.err);
		}
	};

	protected final AtomicInteger threadNumber = new AtomicInteger(0);
	protected final String threadNamePrefix;
	protected final boolean daemonFlag;
	protected final Map<String, String> threadContext;

	public ContextAwareThreadFactory(final String threadNamePrefix, final Map<String, String> threadContext) {
		this.threadNamePrefix = threadNamePrefix;
		this.daemonFlag = false;
		this.threadContext = threadContext;
	}

	public ContextAwareThreadFactory(
		final String threadNamePrefix, final boolean daemonFlag, final Map<String, String> threadContext
	) {
		this.threadNamePrefix = threadNamePrefix;
		this.daemonFlag = daemonFlag;
		this.threadContext = threadContext;
	}

	public static class ContextAwareThread
	extends Thread {

		protected final Map<String, String> threadContext;

		protected ContextAwareThread(
			final Runnable task, final String name, final boolean daemonFlag,
			final UncaughtExceptionHandler exceptionHandler, final Map<String, String> threadContext
		) {
			super(task, name);
			setDaemon(daemonFlag);
			setUncaughtExceptionHandler(exceptionHandler);
			this.threadContext = threadContext;
		}
	}

	@Override
	public Thread newThread(final Runnable task) {
		return new ContextAwareThread(
			task, threadNamePrefix + "#" + threadNumber.incrementAndGet(), daemonFlag, exceptionHandler, threadContext
		);
	}

	@Override
	public String toString() {
		return threadNamePrefix;
	}
}
