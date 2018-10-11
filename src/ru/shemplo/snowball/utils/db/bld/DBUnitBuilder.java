package ru.shemplo.snowball.utils.db.bld;

import ru.shemplo.snowball.utils.db.DBFunction;
import ru.shemplo.snowball.utils.db.DBPrimitive;
import ru.shemplo.snowball.utils.db.bld.DBUnit.DBConstValue;

public class DBUnitBuilder {

	/* ╔════════╗
	 * ║ COLUMN ║
	 * ╚════════╝
	 */
	
	// This is for CREATE TABLE ([column]*)
	public static DBColumnBuilder column (String name, DBPrimitive type, int length, DBColumnFlag ... flags) {
		return new DBColumnBuilder (name, type, length, flags);
	}
	
	public static DBColumnBuilder uC (String name, DBPrimitive type, int length, DBColumnFlag ... flags) {
		return column (name, type, length, flags);
	}
	
	public static DBColumnBuilder column (String name, DBPrimitive type, DBColumnFlag ... flags) {
		return column (name, type, type.DEFAULT_LENGTH, flags);
	}
	
	public static DBColumnBuilder uC (String name, DBPrimitive type, DBColumnFlag ... flags) {
		return column (name, type, flags);
	}
	
	// This is for SELECT [column]* FROM ...
	public static DBColumnBuilder column (String name) {
		return new DBColumnBuilder (name);
	}
	
	public static DBColumnBuilder uC (String name) {
		return column (name);
	}
	
	/* ╔═════╗
	 * ║ KEY ║
	 * ╚═════╝
	 */
	
	
	
	/* ╔══════════╗
	 * ║ FUNCTION ║
	 * ╚══════════╝
	 */
	
	public static DBFunctionBuilder function (DBFunction function, DBUnit ... args) {
		return new DBFunctionBuilder (function, args);
	}
	
	public static DBFunctionBuilder uF (DBFunction function, DBUnit ... args) {
		return function (function, args);
	}
	
	/* ╔═══════╗
	 * ║ VALUE ║
	 * ╚═══════╝
	 */
	
	public static DBConstValue value (String value) {
		return new DBConstValue (value);
	}
	
	public static DBConstValue uV (String value) {
		return value (value);
	}
	
	/* ╔═══════════════════════════════════════════╗
	 * ║ EQUAL, MORE, LESS, MORE EQUAL, LESS EQUAL ║
	 * ╚═══════════════════════════════════════════╝
	 */
	
	public static DBBinaryOperator equal (DBUnit left, DBUnit right) {
		return new DBBinaryOperator (left, " = ", right);
	}
	
	public static DBBinaryOperator more (DBUnit left, DBUnit right) {
		return new DBBinaryOperator (left, " > ", right);
	}
	
	public static DBBinaryOperator less (DBUnit left, DBUnit right) {
		return new DBBinaryOperator (left, " < ", right);
	}
	
	public static DBBinaryOperator moreeq (DBUnit left, DBUnit right) {
		return new DBBinaryOperator (left, " >= ", right);
	}
	
	public static DBBinaryOperator lesseq (DBUnit left, DBUnit right) {
		return new DBBinaryOperator (left, " <= ", right);
	}
	
}
