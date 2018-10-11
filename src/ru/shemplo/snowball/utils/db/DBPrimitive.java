package ru.shemplo.snowball.utils.db;

public enum DBPrimitive {

	INT  (11, false),
	
	BLOB (-1, true),
	TEXT (-1, true);
	
	public final boolean NEED_KEY_LENGTH;
	public final int DEFAULT_LENGTH;
	
	private DBPrimitive (int length, boolean needKeyLength) {
		this.NEED_KEY_LENGTH = needKeyLength;
		this.DEFAULT_LENGTH = length;
	}
	
}
