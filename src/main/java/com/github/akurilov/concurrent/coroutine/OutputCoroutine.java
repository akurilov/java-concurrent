package com.github.akurilov.concurrent.coroutine;

import com.github.akurilov.commons.io.Output;

/**
 * Created by andrey on 29.08.17.
 */
public interface OutputCoroutine<T>
extends Coroutine, Output<T> {
}
