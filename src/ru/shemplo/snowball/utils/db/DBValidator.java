package ru.shemplo.snowball.utils.db;

import java.util.List;

import ru.shemplo.snowball.utils.db.DBQueryBuilder.ColumnBuilder;
import ru.shemplo.snowball.utils.db.DBQueryBuilder.CreateTableBuilder;

@SuppressWarnings ("deprecation")
public class DBValidator {

	public static void testName (String name, DBQueryBuilder owner) {
		if (name == null || name.length () == 0) {
			String text = "Name of " + owner + " is NULL or empty";
			throw new IllegalStateException (text);
		}
	}
	
	public static void testColumns (List <ColumnBuilder <CreateTableBuilder>> columns) {
		if (columns == null || columns.size () == 0) {
			String text = "Table must contains at least one column";
			throw new IllegalStateException (text);
		}
	}
	
	public static void testFunctionArguments (DBFunction function, String ... args) {
		if (function.ARGN == -1 && args.length == 0) {
			String text = "Function " + function + " expects at least one argument";
			throw new IllegalStateException (text);
		}
		
		if (function.ARGN != -1 && args.length != function.ARGN) {
			String text = "Function " + function + " expects " + function.ARGN 
									  + " arguments (" + args.length + " given)";
			throw new IllegalStateException (text);
		}
	}
	
}
