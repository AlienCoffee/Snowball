package ru.shemplo.dsau.utils.time;

import java.util.concurrent.atomic.AtomicLong;

public class TimeoutEvent {
	
	private final AtomicLong LAST_UPDATED = new AtomicLong ();
	private final Runnable EVENT;
	private final long TIMEOUT;
	
	private long timer = 0;
	
	public TimeoutEvent (long timeout, Runnable event) {
		this.LAST_UPDATED.set (System.currentTimeMillis ());
		this.TIMEOUT = timeout;
		this.EVENT = event;
	}
	
	public void update (long delta) {
		long current = System.currentTimeMillis (), 
			 value = LAST_UPDATED.get ();
		
		if (LAST_UPDATED.compareAndSet (value, current)) {
			timer += Math.min (delta + 1, current - value);
			if (timer > TIMEOUT) {
				EVENT.run ();
				timer = 0;
			}
		}
	}
	
}
