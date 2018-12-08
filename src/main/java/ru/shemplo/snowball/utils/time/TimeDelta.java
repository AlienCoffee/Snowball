package ru.shemplo.snowball.utils.time;

import java.util.Date;

import ru.shemplo.snowball.stuctures.Pair;

public class TimeDelta {

	public static enum TDUnit {
		
		MLS  (1000),
		SEC  (60  ),
		MIN  (60  ),
		HWR  (24  ),
		DAY  (0   )
		;
		
		public final long CAPACITY;
		
		private TDUnit (long capacity) {
			this.CAPACITY = capacity;
		}
		
	}
	
	public static TimeDelta valueOf (long milliseconds) {
		return new TimeDelta (milliseconds);
	}
	
	public static TimeDelta valueOf (TDUnit unit, int amount) {
		return new TimeDelta (0).add (unit, amount);
	}
	
	public static TimeDelta deltaOf (Date from, Date to) {
		long delta = to.getTime () - from.getTime ();
		return TimeDelta.valueOf (delta);
	}
	
	public static TimeDelta deltaOfPeriod (Pair <Date, Date> period) {
		return deltaOf (period.F, period.S);
	}
	
	
	private long milliseconds = 0;
	
	public TimeDelta () {
		this (System.currentTimeMillis ());
	}
	
	public TimeDelta (long milliseconds) {
		this.milliseconds = milliseconds;
	}
	
	public String toString () {
		StringBuilder sb = new StringBuilder ("dt = ");
		sb.append (milliseconds >= 0 ? "+" : "-");
		
		int length = TDUnit.values ().length - 1;
		long rest = this.milliseconds;
		
		for (int i = 0; i < length; i++) {
			TDUnit unit = TDUnit.values () [i];
			long value = Math.abs (rest % unit.CAPACITY);
			sb.append (value).append (" ").append (unit).append (", ");
			rest /= unit.CAPACITY;
		}
		
		
		sb.append (rest).append (" ").append (TDUnit.values () [length]);
		
		return sb.toString ();
	}
	
	public long getLength () {
		return Math.abs (this.milliseconds);
	}
	
	public long floorTo (TDUnit unit) {
		if (unit == null) {
			String text = "TDUnit can't be NULL";
			throw new IllegalArgumentException (text);
		}
		
		long result = this.milliseconds;
		for (int i = 0; i < unit.ordinal (); i++) {
			result /= TDUnit.values () [i].CAPACITY;
		}
		
		return result;
	}
	
	public long get (TDUnit unit) {
		boolean isLast = TDUnit.values ().length - 1 == unit.ordinal ();
		return floorTo (unit) % (isLast ? Long.MAX_VALUE : unit.CAPACITY);
	}
	
	public TimePeriod getPeriod () {
		return getPeriodFor (new Date ());
	}
	
	public TimePeriod getPeriodFor (Date from) {
		TimePeriod period = TimePeriod.mtp (from, new Date (from.getTime () + milliseconds));
		
		// Delta time is negative -> result Date will be earlier then given
		if (milliseconds < 0) { period = period.swap (); }
		
		return period;
	}
	
	public TimeDelta add (TDUnit unit, int amount) {
		if (unit == null) {
			String text = "TDUnit can't be NULL";
			throw new IllegalArgumentException (text);
		}
		
		TDUnit current = unit;
		long delta = amount;
		
		while (current.ordinal () > 0) {
			TDUnit less = TDUnit.values () [current.ordinal () - 1];
			delta *= less.CAPACITY;
			current = less;
		}
		
		this.milliseconds += delta;
		
		return this;
	}
	
	public TimeDelta add (TimeDelta delta) {
		return add (delta, true);
	}
	
	public TimeDelta add (TimeDelta delta, boolean positive) {
		this.milliseconds += (positive ? 1 : -1) * delta.milliseconds;
		
		return this;
	}
	
}
