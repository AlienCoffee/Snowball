package ru.shemplo.snowball.utils.db.bld;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import ru.shemplo.snowball.utils.db.DBPrimitive;
import ru.shemplo.snowball.utils.db.DBValidator;

public class DBColumnBuilder implements DBUnit {

	// XXX: in future make protected getters for private fields
	protected String name = "", label = "", collate = "";
	private DBPrimitive type; int typeLength = 0;
	private DBUnit _default = null;
	
	private final Set <DBColumnFlag> FLAGS = new HashSet <> ();
	private final boolean forCreate;
	
	protected DBColumnBuilder (String name, DBPrimitive type, int length, DBColumnFlag ... flags) {
		this.forCreate = true;
		this.name = name;
		
		this.typeLength = length;
		this.type = type;
		
		Arrays.asList (flags).forEach (FLAGS::add);
	}
	
	protected DBColumnBuilder (String name) {
		this.forCreate = false;
		this.name = name;
	}
	
	public String toString () {
		DBValidator.testName (name, null);
		
		StringBuilder sb = new StringBuilder ();
		sb.append ("`").append (name).append ("`");
		
		if (forCreate) {
			sb.append (" ").append (type.name ().toLowerCase ());
			if (typeLength != type.DEFAULT_LENGTH) {
				sb.append ("(").append (typeLength).append (")");
			}
			
			FLAGS.stream ().forEach (f -> sb.append (" ").append (f.QUERY));
			
			if (collate.length () > 0) {
				sb.append (" COLLATE '").append (collate).append ("'");
			}
			if (_default != null) {
				sb.append (" DEFAULT '").append (_default)
										.append ("'");
			}
		} else {
			if (label.length () > 0) {
				sb.append (" AS '").append (label).append ("'");
			}
		}
		
		return sb.toString ();
	}
	
	public DBColumnBuilder defV (DBUnit defaultValue) {
		this._default = defaultValue;
		return this;
	}
	
	public DBColumnBuilder collate (String encoding) {
		if (encoding == null || encoding.length () == 0) {
			String text = "Collate value can't be EMPTY value";
			throw new IllegalArgumentException (text);
		}
		
		this.collate = encoding;
		return this;
	}
	
	public DBColumnBuilder as (String label) {
		if (label == null || label.length () == 0) {
			String text = "AS label can't have EMPTY value";
			throw new IllegalArgumentException (text);
		}
		
		this.label = label;
		return this;
	}
	
}
