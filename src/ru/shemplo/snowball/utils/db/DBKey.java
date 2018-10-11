package ru.shemplo.snowball.utils.db;

public enum DBKey {

	PRIMARY  (false, false),
	UNIQUE   ( true, false),
	INDEX    (false, false),
	FULLTEXT ( true, false),
	SPATIAL  (false, true );
	
	public final boolean CAN_INDEX, CAN_REFERENCE;
	
	private DBKey (boolean canIndex, boolean canReference) {
		this.CAN_REFERENCE = canReference;
		this.CAN_INDEX = canIndex;
	}
	
}
