package com.github.akurilov.concurrent.coroutine;

import com.github.akurilov.concurrent.ContextAwareThreadFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

/**
 * The coroutines executor. It's suggested to use a single/global/shared executor instance per
 * application. By default the background coroutines executor is created. The normal coroutines
 * executor with higher scheduling priority may be created using the custom constructor with
 * <i>false</i> argument.
 */
public class CoroutinesExecutor {

	private final static Logger LOG = Logger.getLogger(CoroutinesExecutor.class.getName());

	private final ThreadPoolExecutor executor;
	private final boolean backgroundFlag;
	private final List<CoroutinesExecutorTask> workers = new ArrayList<>();
	private final Queue<Coroutine> coroutines = new ConcurrentLinkedQueue<>();

	public CoroutinesExecutor() {
		this(true);
	}

	public CoroutinesExecutor(final boolean backgroundFlag) {
		final int svcThreadCount = Runtime.getRuntime().availableProcessors();
		executor = new ThreadPoolExecutor(
			svcThreadCount, svcThreadCount, 0, TimeUnit.DAYS, new ArrayBlockingQueue<>(1),
			new ContextAwareThreadFactory("coroutine-processor-", true, null)
		);
		this.backgroundFlag = backgroundFlag;
		for(int i = 0; i < svcThreadCount; i ++) {
			final CoroutinesExecutorTask svcWorkerTask = new CoroutinesExecutorTask(
				coroutines, backgroundFlag
			);
			executor.submit(svcWorkerTask);
			workers.add(svcWorkerTask);
			svcWorkerTask.start();
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
					final CoroutinesExecutorTask execTask = new CoroutinesExecutorTask(
						coroutines, backgroundFlag
					);
					executor.submit(execTask);
					workers.add(execTask);
					execTask.start();
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
}
