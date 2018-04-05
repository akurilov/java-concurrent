package com.github.akurilov.concurrent;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 Modifies the async runnable to make sure that all instances are closed by the special method.
 It's useful to place the closeAll() call into the JVM's shutdown hook.
 */
public abstract class DaemonBase
extends AsyncRunnableBase {

	private static final Logger LOG = Logger.getLogger(DaemonBase.class.getName());

	private static final Queue<AsyncRunnable> REGISTRY = new ConcurrentLinkedQueue<>();

	protected  DaemonBase() {
		REGISTRY.add(this);
	}

	@Override
	public final void close()
	throws IOException {
		REGISTRY.remove(this);
		super.close();
	}

	public static void closeAll() {
		while(!REGISTRY.isEmpty()) {
			try {
				REGISTRY.peek().close();
			} catch(final Throwable cause) {
				LOG.log(Level.WARNING, "Failed to close the daemon instance", cause);
			}
		}
	}
}
