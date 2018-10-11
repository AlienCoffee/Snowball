package ru.shemplo.snowball.utils.db.bld;

import ru.shemplo.snowball.utils.db.DBKey;

public class DBKeyBuilder implements DBUnit {

	protected DBKeyBuilder (DBKey key) {
		
	}
	
	public DBKeyBuilder len (int length) {
		return this;
	}
	
	public DBKeyBuilder refs () {
		return this;
	}
	
	@Override
	public DBUnit as (String label) {
		return null;
	}
	
}
