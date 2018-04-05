package com.github.akurilov.concurrent;

/**
 * Something what may be initialized once
 */
public interface Initializable {

	/**
	 * @return true if initialized, false otherwise
	 */
	boolean isInitialized();
}
