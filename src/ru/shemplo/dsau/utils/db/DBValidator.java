package ru.shemplo.dsau.utils.db;

public class DBValidator {

	public static void testName (String name, DBQueryBuilder owner) {
		if (name == null || name.length () == 0) {
			String text = "Name of " + owner.getClass ().getSimpleName () + " is NULL or empty";
			throw new IllegalStateException (text);
		}
	}
	
}
