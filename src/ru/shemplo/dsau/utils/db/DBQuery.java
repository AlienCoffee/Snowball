package ru.shemplo.dsau.utils.db;

public class DBQuery {

	private final String QUERY;
	
	public DBQuery (String query) {
		this.QUERY = query;
	}
	
	public String toString () {
		return QUERY;
	}
	
}
