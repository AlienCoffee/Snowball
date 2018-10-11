package ru.shemplo.snowball.utils.db.bld;

public enum DBColumnFlag {

	AUTO_INC ("AUTO_INCREMENT"),
	NOT_NULL ("NOT NULL"),
	UNSIGNED ("unsigned"),
	ZEROFILL ("zerofill");
	
	public final String QUERY;
	
	private DBColumnFlag (String query) {
		this.QUERY = query;
	}
	
}
