package com.github.akurilov.concurrent.throttle;

import org.junit.Test;
import static org.junit.Assert.assertEquals;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.concurrent.locks.LockSupport;

/**
 Created by andrey on 06.11.16.
 */

public class SequentialWeightsThrottleTest {

	private static final int WRITE = 0;
	private static final int READ = 1;

	private final int[] weights = new int[] {
		80,
		20
	};
	private final LongAdder[] resultCounters = new LongAdder[] {
		new LongAdder(),
		new LongAdder()
	};

	private final SequentialWeightsThrottle wt = new SequentialWeightsThrottle(weights);

	private final class SubmTask
		implements Runnable {
		private final int origin;
		public SubmTask(final int origin) {
			this.origin = origin;
		}
		@Override
		public final void run() {
			while(true) {
				if(wt.tryAcquire(origin)) {
					resultCounters[origin].increment();
				} else {
					LockSupport.parkNanos(1);
				}
			}
		}
	}

	@Test
	public void testRequestApprovalFor()
	throws Exception {
		final ExecutorService es = Executors.newFixedThreadPool(2);
		es.submit(new SubmTask(WRITE));
		es.submit(new SubmTask(READ));
		es.awaitTermination(10, TimeUnit.SECONDS);
		es.shutdownNow();
		final double writes = resultCounters[WRITE].sum();
		final long reads = resultCounters[READ].sum();
		assertEquals(80/20, writes / reads, 0.01);
		System.out.println("Write rate: " + writes / 10 + " Hz, read rate: " + reads / 10 + " Hz");
	}

	private final class BatchSubmTask
	implements Runnable {
		private final int origin;
		public BatchSubmTask(final int origin) {
			this.origin = origin;
		}
		@Override
		public final void run() {
			int n;
			while(true) {
				n = wt.tryAcquire(origin, 128);
				if(n > 0) {
					resultCounters[origin].add(n);
				} else {
					LockSupport.parkNanos(1);
				}
			}
		}
	}

	@Test
	public void testRequestBatchApprovalFor()
	throws Exception {
		final ExecutorService es = Executors.newFixedThreadPool(2);
		es.submit(new BatchSubmTask(WRITE));
		es.submit(new BatchSubmTask(READ));
		es.awaitTermination(10, TimeUnit.SECONDS);
		es.shutdownNow();
		final double writes = resultCounters[WRITE].sum();
		final long reads = resultCounters[READ].sum();
		assertEquals(80/20, writes / reads, 0.01);
		System.out.println("Write rate: " + writes / 10 + " Hz, read rate: " + reads / 10 + " Hz");
	}
}
