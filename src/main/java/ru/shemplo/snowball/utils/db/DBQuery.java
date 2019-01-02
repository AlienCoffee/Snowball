package ru.shemplo.snowball.utils.db;

public class DBQuery {

	private final String QUERY;
	
	public DBQuery (String query) {
		this.QUERY = query;
	}
	
	@Override
    public String toString () {
		return QUERY;
	}
	
}
