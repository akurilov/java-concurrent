package com.github.akurilov.concurrent;

import java.util.Arrays;

/**
 Created by kurila on 29.03.16.
 An throttle which uses the map of weights.
 The throttle determines the weight for each I/O task and makes the decision.
 The weight is used to pass the I/O task with specific ratio for the different keys.
 */
public final class SequentialWeightsThrottle {

	// initial weight map (constant)
	private final int[] weights;
	private final int[] remainingWeights;

	public SequentialWeightsThrottle(final int[] weights)
	throws IllegalArgumentException {
		this.weights = Arrays.copyOf(weights, weights.length);
		remainingWeights = new int[weights.length];
		resetRemainingWeights();
	}

	private void resetRemainingWeights()
	throws IllegalArgumentException {
		for(int i = 0; i < weights.length; i ++) {
			remainingWeights[i] = weights[i];
		}
	}

	private void ensureRemainingWeights() {
		for(int i = 0; i < weights.length; i ++) {
			if(remainingWeights[i] > 0) {
				return;
			}
		}
		resetRemainingWeights();
	}

	public final boolean tryAcquire(final int index) {
		synchronized(remainingWeights) {
			ensureRemainingWeights();
			final int remainingWeight = remainingWeights[index];
			if(remainingWeight > 0) {
				remainingWeights[index] = remainingWeight - 1;
				return true;
			} else {
				return false;
			}
		}
	}

	public final int tryAcquire(final int index, final int times) {
		if(times == 0) {
			return 0;
		}
		synchronized(remainingWeights) {
			ensureRemainingWeights();
			final int remainingWeight = remainingWeights[index];
			if(times > remainingWeight) {
				remainingWeights[index] = 0;
				return remainingWeight;
			} else {
				remainingWeights[index] = remainingWeight - times;
				return times;
			}
		}
	}
}
