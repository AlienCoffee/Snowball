package ru.shemplo.snowball.utils.db;

public enum DBPrimitive {

	INT (11), TEXT (-1);
	
	public final int DEFAULT_LENGTH;
	
	private DBPrimitive (int length) {
		this.DEFAULT_LENGTH = length;
	}
	
}
