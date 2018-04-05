package com.github.akurilov.concurrent;

/**
 A {@link Runnable} which can be initialized and should be initialized before being invoked.
 */
public interface InitRunnable
extends Initializable, Runnable {
}
