package ru.shemplo.snowball.utils.db;

public enum DBType {

	SQLite ("sqlite"),
	MySQL  ("mysql")
	;
	
	public final String TYPE;
	
	private DBType (String type) {
		this.TYPE = type;
	}
	
}
