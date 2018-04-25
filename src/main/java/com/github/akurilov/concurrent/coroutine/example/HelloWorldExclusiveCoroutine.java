package com.github.akurilov.concurrent.coroutine.example;

import com.github.akurilov.concurrent.coroutine.CoroutinesExecutor;
import com.github.akurilov.concurrent.coroutine.ExclusiveCoroutineBase;

import java.io.IOException;

/**
 * Created by andrey on 23.08.17.
 */
public class HelloWorldExclusiveCoroutine
extends ExclusiveCoroutineBase {

	public HelloWorldExclusiveCoroutine(final CoroutinesExecutor executor) {
		super(executor);
	}

	@Override
	protected void invokeTimedExclusively(long startTimeNanos) {
		System.out.println("Hello world!");
	}

	@Override
	protected void doClose()
	throws IOException {

	}
}
