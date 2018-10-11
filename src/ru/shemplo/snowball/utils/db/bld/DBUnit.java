package ru.shemplo.snowball.utils.db.bld;

import ru.shemplo.snowball.utils.db.DBValidator;

public interface DBUnit {

	public DBUnit as (String label);
	
	public static class DBConstValue implements DBUnit {

		protected final String VALUE;
		
		private String label = "";
		
		public DBConstValue (String value) {
			DBValidator.testName (value, null);
			this.VALUE = value;
		}
		
		@Override
		public String toString () {
			StringBuilder sb = new StringBuilder (VALUE);
			if (label.length () > 0) {
				sb.append ("AS '").append (label).append ("'");
			}
			
			return sb.toString ();
		}
		
		@Override
		public DBUnit as (String label) {
			DBValidator.testName (label, null);
			this.label = label;
			return this;
		}
		
	}
		
}
