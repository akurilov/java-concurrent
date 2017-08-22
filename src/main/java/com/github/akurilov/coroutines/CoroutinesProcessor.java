package com.github.akurilov.coroutines;

import com.github.akurilov.commons.concurrent.StoppableTask;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * The coroutines executor. It's suggested to use a single/global/shared executor instance per application.
 */
public class CoroutinesProcessor {

	private final ThreadPoolExecutor executor;
	private final List<StoppableTask> workers = new ArrayList<>();
	private final Queue<Coroutine> coroutines = new ConcurrentLinkedQueue<>();

	public CoroutinesProcessor() {
		final int svcThreadCount = Runtime.getRuntime().availableProcessors();
		executor = new ThreadPoolExecutor(
			svcThreadCount, svcThreadCount, 0, TimeUnit.DAYS, new ArrayBlockingQueue<>(1)
		);
		for(int i = 0; i < svcThreadCount; i ++) {
			final StoppableTask svcWorkerTask = new CoroutinesProcessorTask(coroutines);
			executor.submit(svcWorkerTask);
			workers.add(svcWorkerTask);
		}
	}

	public void start(final Coroutine coroutine) {
		coroutines.add(coroutine);
	}

	public void stop(final Coroutine coroutine) {
		coroutines.remove(coroutine);
	}

	public void setThreadCount(final int threadCount) {
		final int newThreadCount = threadCount > 0 ?
			threadCount : Runtime.getRuntime().availableProcessors();
		final int oldThreadCount = executor.getCorePoolSize();
		if(newThreadCount != oldThreadCount) {
			executor.setCorePoolSize(newThreadCount);
			executor.setMaximumPoolSize(newThreadCount);
			if(newThreadCount > oldThreadCount) {
				for(int i = oldThreadCount; i < newThreadCount; i ++) {
					final StoppableTask procTask = new CoroutinesProcessorTask(coroutines);
					executor.submit(procTask);
					workers.add(procTask);
				}
			} else { // less, remove some active service worker tasks
				try {
					for(int i = oldThreadCount - 1; i >= newThreadCount; i --) {
						workers.remove(i).close();
					}
				} catch (final Exception e) {
					e.printStackTrace(System.err);
				}
			}
		}
	}

	private static final class CoroutinesProcessorTask
	implements StoppableTask {

		private final Queue<Coroutine> coroutines;

		private volatile boolean closedFlag = false;

		private CoroutinesProcessorTask(final Queue<Coroutine> coroutines) {
			this.coroutines = coroutines;
		}

		@Override
		public final void run() {
			while(!closedFlag) {
				if(coroutines.size() == 0) {
					try {
						Thread.sleep(1);
					} catch(final InterruptedException e) {
						break;
					}
				} else {
					for(final Coroutine nextCoroutine : coroutines) {
						if(!nextCoroutine.isClosed()) {
							try {
								nextCoroutine.run();
							} catch(final Throwable t) {
								synchronized(System.err) {
									System.err.println("Coroutine \"" + nextCoroutine + "\" failed:");
									t.printStackTrace(System.err);
								}
							}
							//LockSupport.parkNanos(1);
						}
					}
				}
			}
		}

		@Override
		public final boolean isClosed() {
			return closedFlag;
		}

		@Override
		public final void close() {
			closedFlag = true;
		}
	}
}
