package ru.shemplo.snowball.utils.db.bld;

public class DBCreateBuilder {

	/* ╔══════════════╗
	 * ║ CREATE TABLE ║
	 * ╚══════════════╝
	 */
	public static DBCreateTableBuilder table (String name) {
		return new DBCreateTableBuilder (name);
	}
	
}
