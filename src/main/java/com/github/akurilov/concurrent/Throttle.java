package com.github.akurilov.concurrent;

/**
 Throttle can make a decision about the specified thing to pass or to wait.
 The throttle calls are not blocking so the caller should block if the throttle tells so.
 */
public interface Throttle<X> {

	/**
	 Request a permit about a thing
	 @param thing the subject of the permit
	 @return true if the thing should be passed, false otherwise
	 */
	boolean tryAcquire(final X thing);

	/**
	 Request permits about a set of things
	 @param thing the subject of the permits
	 @param times how many permits is requested
	 @return how many permits are got
	 */
	int tryAcquire(final X thing, int times);
}
