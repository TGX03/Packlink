package de.tgx03;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * A class allowing a thread to sleep until a certain value has been reached while still allowing to increase the number.
 */
public class ThreadWaiter {

	/**
	 * Where the counter is currently at.
	 */
	private final AtomicInteger currentNumber = new AtomicInteger();
	/**
	 * The number to be reached.
	 */
	private volatile int threshold;

	/**
	 * Create a new waiter with an initial threshold of 0.
	 */
	public ThreadWaiter() {
		threshold = 0;
	}

	/**
	 * Creates a new waiter with a specified threshold.
	 *
	 * @param startThreshold The specified threshold.
	 */
	public ThreadWaiter(int startThreshold) {
		threshold = startThreshold;
	}

	/**
	 * Increment the current value.
	 */
	public void increment() {
		if (threshold == currentNumber.incrementAndGet()) {
			synchronized (this) {
				notifyAll();
			}
		}
	}

	/**
	 * Increment the threshold.
	 */
	public synchronized void incrementThreshold() {
		threshold++;
	}

	/**
	 * Allows a thread to wait until the threshold has been reached.
	 *
	 * @throws InterruptedException I dunno.
	 */
	public synchronized void await() throws InterruptedException {
		while (threshold != currentNumber.get()) {
			wait();
		}
	}
}