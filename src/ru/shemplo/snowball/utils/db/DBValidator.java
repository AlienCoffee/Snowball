package ru.shemplo.snowball.utils.db;

import java.util.List;

import ru.shemplo.snowball.utils.db.DBQueryBuilder.ColumnBuilder;
import ru.shemplo.snowball.utils.db.DBQueryBuilder.CreateTableBuilder;

public class DBValidator {

	public static void testName (String name, DBQueryBuilder owner) {
		if (name == null || name.length () == 0) {
			String text = "Name of " + owner.getClass ().getSimpleName () + " is NULL or empty";
			throw new IllegalStateException (text);
		}
	}
	
	public static void testColumns (List <ColumnBuilder <CreateTableBuilder>> columns) {
		if (columns == null || columns.size () == 0) {
			String text = "Table must contains at least one column";
			throw new IllegalStateException (text);
		}
	}
	
}
