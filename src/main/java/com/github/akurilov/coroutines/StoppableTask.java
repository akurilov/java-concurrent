package com.github.akurilov.coroutines;

import java.io.Closeable;
import java.io.IOException;

/**
 * A runnable task which can be stopped
 */
public interface StoppableTask
extends Closeable, Runnable {

	int CLOSE_TIMEOUT_NANOS = 1_000_000_000;

	/**
	 * Stop the task somehow
	 */
	void stop();

	boolean isStopped();

	/**
	 * Also stops the task if not stopped yet
	 * @throws IOException if some kind of failure occured
	 */
	@Override
	void close()
	throws IOException;

	/**
	 * @return true if the task was once stopped, false otherwise
	 */
	boolean isClosed();
}
