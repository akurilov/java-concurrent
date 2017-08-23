package com.github.akurilov.coroutines.example;

import com.github.akurilov.coroutines.CoroutineBase;
import com.github.akurilov.coroutines.CoroutinesProcessor;

import java.io.IOException;

/**
 * Created by andrey on 23.08.17.
 */
public class HelloWorldCoroutine
extends CoroutineBase {

	public HelloWorldCoroutine(final CoroutinesProcessor coroutinesProcessor) {
		super(coroutinesProcessor);
	}

	@Override
	protected void invokeTimed(long startTimeNanos) {
		System.out.println("Hello world!");
	}

	@Override
	protected void doClose()
	throws IOException {

	}
}
