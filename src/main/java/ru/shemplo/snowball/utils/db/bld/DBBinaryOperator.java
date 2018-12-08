package ru.shemplo.snowball.utils.db.bld;

public class DBBinaryOperator implements DBUnit {

	protected final DBUnit LEFT, RIGHT;
	protected final String OPER;
	
	private String label = "";
	
	protected DBBinaryOperator (DBUnit left, String operator, DBUnit right) {
		this (operator, left, right);
	}
	
	protected DBBinaryOperator (String operator, DBUnit left, DBUnit right) {
		this.LEFT = left; this.RIGHT = right;
		this.OPER = operator;
	}
	
	public String toString () {
		StringBuilder sb = new StringBuilder ();
		sb.append (LEFT).append (OPER).append (RIGHT);
		if (label.length () > 0) {
			sb.append ("AS '").append (label).append ("'");
		}
		
		return  sb.toString ();
	}

	@Override
	public DBUnit as (String label) {
		if (label == null || label.length () == 0) {
			String text = "AS label can't have EMPTY value";
			throw new IllegalArgumentException (text);
		}
		
		this.label = label;
		return this;
	}
	
}
