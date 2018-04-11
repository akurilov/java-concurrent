module com.github.akurilov.concurrent {

	requires com.github.akurilov.commons;
	requires commons.collections4;
	requires java.base;
	requires java.logging;
	requires java.rmi;

	exports com.github.akurilov.concurrent;
	exports com.github.akurilov.concurrent.coroutines;
	exports it.unimi.dsi.fastutil;
	exports it.unimi.dsi.fastutil.booleans;
	exports it.unimi.dsi.fastutil.bytes;
	exports it.unimi.dsi.fastutil.ints;
	exports it.unimi.dsi.fastutil.objects;
	exports it.unimi.dsi.fastutil.shorts;
}