package com.github.akurilov.coroutines;

//import com.codahale.metrics.Counter;
//import com.codahale.metrics.Histogram;
//import com.codahale.metrics.JmxReporter;
//import com.codahale.metrics.MetricRegistry;

import com.github.akurilov.commons.concurrent.StoppableTaskBase;

import java.io.IOException;

//import com.emc.mongoose.api.model.svc.ServiceUtil;

/**
 * The base class for all coroutines.
 */
public abstract class CoroutineBase
extends StoppableTaskBase
implements Coroutine {

	//private final static MetricRegistry METRIC_REGISTRY = new MetricRegistry();
	//private final static JmxReporter METRIC_REPORTER = JmxReporter
	//	.forRegistry(METRIC_REGISTRY)
	//	.inDomain(Coroutine.class.getPackage().getName())
	//	.registerWith(ServiceUtil.MBEAN_SERVER)
	//	.build();
	//static {
	//	METRIC_REPORTER.start();
	//}

	private final CoroutinesProcessor coroutinesProcessor;
	//private final Histogram durations;
	//private final Counter durationsSum;

	protected CoroutineBase(final CoroutinesProcessor coroutinesProcessor) {
		this.coroutinesProcessor = coroutinesProcessor;
		//this.durations = METRIC_REGISTRY.histogram(getClass().getSimpleName() + "-durations");
		//this.durationsSum = METRIC_REGISTRY.counter(getClass().getSimpleName() + "-durationsSum");
	}

	@Override
	public final void start() {
		coroutinesProcessor.start(this);
	}

	/**
	 * Decorates the invocation method with timing.
	 */
	@Override
	protected final void invoke() {
		long t = System.nanoTime();
		invokeTimed(t);
		//t = System.nanoTime() - t;
		//if(t > TIMEOUT_NANOS) {
		//	System.err.println(
		//		"Coroutine \"" + toString() + "\" invocation duration exceeded the limit: " + t +
		//			"[ns]"
		//	);
		//}
		//durations.update(t);
		//durationsSum.inc();
	}

	/**
	 * The method implementation should use the start time to check its own duration in order to not to exceed the
	 * invocation time limit (250ms)
	 * @param startTimeNanos the time when the invocation started
	 */
	protected abstract void invokeTimed(final long startTimeNanos);

	@Override
	public final void close()
	throws IOException {
		coroutinesProcessor.stop(this);
		super.close();
	}
}
