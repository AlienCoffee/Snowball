package ru.shemplo.snowball.utils.db.bld;

import static ru.shemplo.snowball.utils.db.bld.DBCreateFlag.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringJoiner;

import ru.shemplo.snowball.utils.db.DBEngine;

public class DBCreateTableBuilder {

	private final Map <String, DBColumnBuilder> COLS = new HashMap <> ();
	private final List <String> COLS_ORDER = new ArrayList <> ();
	private final Set <DBCreateFlag> FLAGS = new HashSet <> ();
	private final String NAME;
	
	private String like = null, collate = null;
	private DBEngine engine;
	
	protected DBCreateTableBuilder (String name) {
		this.NAME = name;
	}
	
	@Override
	public String toString () {
		StringBuilder sb = new StringBuilder ();
		
		sb.append ("CREATE");
		if (FLAGS.contains (TEMPORARY)) {
			sb.append (" ").append (TEMPORARY);
		}
		
		sb.append (" TABLE");
		if (FLAGS.contains (IF_NOT_EXISTS)) {
			sb.append (" ").append (IF_NOT_EXISTS);
		}
		
		sb.append (" `").append (NAME).append ("`");
		
		if (like != null && like.length () > 0) {
			sb.append (" LIKE `").append (like).append ("`");
			return sb.toString ();
		}
		
		sb.append (" (");
		StringJoiner joiner = new StringJoiner (", ");
		COLS_ORDER.stream ().map (COLS::get).map (Objects::toString).forEach (joiner::add);
		sb.append (joiner.toString ());
		
		sb.append (")");
		if (engine != null) { 
			sb.append (" ENGINE='").append (engine).append ("'");
		}
		
		if (collate != null && collate.length () > 0) {
			sb.append (" COLLATE '").append (collate).append ("'");
		}
		
		return sb.toString ();
	}
	
	public DBCreateTableBuilder like (String name) {
		this.like = name;
		return this;
	}
	
	public DBCreateTableBuilder columns (DBColumnBuilder ... columns) {
		Arrays.asList (columns).forEach (c -> {
			if (COLS.put (c.name, c) == null) {
				COLS_ORDER.add (c.name);
			}
		});
		return this;
	}
	
	public DBCreateTableBuilder flags (DBCreateFlag ... flags) {
		Arrays.asList (flags).forEach (FLAGS::add);
		return this;
	}
	
	public DBCreateTableBuilder keys () {
		return this;
	}
	
	public DBCreateTableBuilder collate (String encoding) {
		this.collate = encoding;
		return this;
	}
	
	public DBCreateTableBuilder engine (DBEngine engine) {
		if (engine == null) { /*...*/ } 
		this.engine = engine;
		return this;
	}
	
}
