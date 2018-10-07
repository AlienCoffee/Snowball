package ru.shemplo.dsau.utils.time;

import java.util.Date;

public class TimeUtils {

	public static Date floorToHours (Date date) {
		return floorToHours (date.getTime ());
	}
	
	public static Date floorToHours (long time) {
		long mod = 1000 * 60 * 60;
		return new Date (time - time % mod);
	}
	
	public static Date floorToDays (Date date) {
		long time = date.getTime (), mod = 1000 * 60 * 60 * 24;
		return new Date (time - time % mod);
	}
	
}
