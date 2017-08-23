package com.github.akurilov.coroutines.example;

import com.github.akurilov.coroutines.Coroutine;
import com.github.akurilov.coroutines.CoroutinesProcessor;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * Created by andrey on 23.08.17.
 */
public class Main {

	public static void main(final String... args)
	throws InterruptedException, IOException {

		final CoroutinesProcessor coroutinesProcessor = new CoroutinesProcessor();
		final Coroutine helloCoroutine = new HelloWorldCoroutine(coroutinesProcessor);
		helloCoroutine.start();
		TimeUnit.SECONDS.sleep(10);
		helloCoroutine.close();
	}
}
