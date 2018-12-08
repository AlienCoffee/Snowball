package ru.shemplo.snowball.utils.db.bld;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

import ru.shemplo.snowball.utils.db.DBFunction;

public class DBFunctionBuilder implements DBUnit {

	protected final List <DBUnit> ARGS = new ArrayList <> ();
	protected final DBFunction F;
	
	private String label = "";
	
	protected DBFunctionBuilder (DBFunction function, DBUnit ... args) {
		if (function == null) {
			String text = "Function token can't have NULL value";
			throw new IllegalArgumentException (text);
		}
		
		ARGS.addAll (Arrays.asList (args));
		this.F = function;
	}
	
	@Override
	public String toString () {
		if (F.ARGN != -1 && F.ARGN != ARGS.size ()) {
			String text = "Function " + F + " requires " 
								+ F.ARGN + " arguments";
			throw new IllegalArgumentException (text);
		}
		
		StringJoiner joiner = new StringJoiner (", ");
		ARGS.forEach (a -> joiner.add (a.toString ()));
		
		StringBuilder sb = new StringBuilder (F.name ());
		sb.append ("(").append (joiner.toString ()).append (")");
		
		if (label.length () > 0) {
			sb.append ("AS '").append (label).append ("'");
		}
		
		return sb.toString ();
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
