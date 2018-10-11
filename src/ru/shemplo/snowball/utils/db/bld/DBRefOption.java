package ru.shemplo.snowball.utils.db.bld;

public enum DBRefOption {

	RESTRICT ("RESTRICT"), 
	CASCADE  ("CASCADE"), 
	SET_NULL ("SET NULL"), 
	NO_ACT   ("NO ACTION"), 
	SET_DEF  ("SET DEFAULT");
	
	public final String QUERY;
	
	private DBRefOption (String query) {
		this.QUERY = query;
	}
	
}
