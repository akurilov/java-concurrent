package com.github.akurilov.concurrent.throttle;

/**
 Throttle can make a decision about the specified thing to pass or to wait.
 The throttle calls are not blocking so the caller should block if the throttle tells so.
 */
public interface Throttle {

	/**
	 Request a permit about a thing
	 @return true if the thing should be passed, false otherwise
	 */
	boolean tryAcquire();

	/**
	 Request permits about a set of things
	 @param times how many permits is requested
	 @return how many permits are got
	 */
	int tryAcquire(int times);
}
