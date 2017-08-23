package com.github.akurilov.coroutines.example;

import com.github.akurilov.coroutines.CoroutinesProcessor;
import com.github.akurilov.coroutines.ExclusiveCoroutineBase;

import java.io.IOException;

/**
 * Created by andrey on 23.08.17.
 */
public class HelloWorldExclusiveCoroutine
extends ExclusiveCoroutineBase {

	public HelloWorldExclusiveCoroutine(final CoroutinesProcessor coroutinesProcessor) {
		super(coroutinesProcessor);
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
