package com.github.akurilov.concurrent;

import java.util.concurrent.Callable;

/**
 A {@link Callable} which can be initialized and should be initialized before being invoked.
 */
public interface InitCallable<V>
extends Initializable, Callable<V> {
}
