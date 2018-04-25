package com.github.akurilov.concurrent.throttle;

public interface IndexThrottle {

	boolean tryAcquire(final int index);

	int tryAcquire(final int index, final int times);
}
