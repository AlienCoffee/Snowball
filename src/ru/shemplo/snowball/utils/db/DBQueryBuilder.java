package ru.shemplo.snowball.utils.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

import ru.shemplo.snowball.stuctures.Pair;

public abstract class DBQueryBuilder {
	
	private DBQueryBuilder () {}
	
	public abstract DBQuery asQuery ();
	
	//-------------------------------//
	
	//-------------------------------//
	
	public static class ColumnBuilder <Owner extends DBQueryBuilder> extends DBQueryBuilder {
		
		private final Owner OWNER;
		
		private boolean isNotNull = false;
		private DBPrimitive type;
		private String name;
		
		public ColumnBuilder (Owner owner, String name) {
			this.OWNER = owner;
			this.name = name;
		}
		
		public ColumnBuilder <Owner> name (String name) {
			this.name = name;
			return this;
		}
		
		public ColumnBuilder <Owner> type (DBPrimitive type) {
			this.type = type;
			return this;
		}
		
		public ColumnBuilder <Owner> notNull () {
			this.isNotNull = true;
			return this;
		}
		
		public Owner add () {
			return OWNER;
		}

		@Override
		public DBQuery asQuery () {
			DBValidator.testName (name, this);
			
			StringBuilder sb = new StringBuilder ();
			sb.append ("'").append (name).append ("' ").append (type);
			if (isNotNull) {
				sb.append (" NOT NULL");
			}
			
			return new DBQuery (sb.toString ());
		}
		
	}
	
	/* 
	 * ╔═════════════════════╗
	 * ║                     ║
	 * ║ CREATE BUILDER PART ║
	 * ║                     ║
	 * ╚═════════════════════╝
	 * 
	 * Documentation:
	 * https://phpclub.ru/mysql/doc/create-table.html
	 * 
	 */
	
	public static CreateBuilder buildCREATE () {
		return new CreateBuilder ();
	}
	
	public static class CreateBuilder {
		
		private CreateBuilder () {}
		
		public CreateTableBuilder table (String name) { return new CreateTableBuilder (name); }
		public CreateTableBuilder table (           ) { return new CreateTableBuilder ("");   }
		
		
		
	}
	
	public static class CreateTableBuilder extends DBQueryBuilder {

		private final List <ColumnBuilder <CreateTableBuilder>> 
			COLUMNS = new ArrayList <> ();
		private final List <Pair <DBKey, String []>>
			KEYS = new ArrayList <> ();
		
		private boolean onlyOnAbset = false, 
						isTemporary = false;
		private String name, like = null;
		
		private CreateTableBuilder (String name) {
			this.name = name;
		}
		
		public CreateTableBuilder temporary () {
			this.isTemporary = true;
			return this;
		}
		
		public CreateTableBuilder onAbsent () {
			this.onlyOnAbset = true;
			return this;
		}
		
		public CreateTableBuilder name (String name) {
			this.name = name;
			return this;
		}
		
		public CreateTableBuilder like (String name) {
			this.like = name;
			return this;
		}
		
		public ColumnBuilder <CreateTableBuilder> column () {
			ColumnBuilder <CreateTableBuilder> 
				builder = new ColumnBuilder <> (this, "");
			
			COLUMNS.add (builder);
			return builder;
		}
		
		public ColumnBuilder <CreateTableBuilder> column (String name) {
			ColumnBuilder <CreateTableBuilder> builder = column ();
			builder.name (name);
			return builder;
		}
		
		public ColumnBuilder <CreateTableBuilder> column (String name, DBPrimitive type) {
			ColumnBuilder <CreateTableBuilder> builder = column (name);
			builder.type (type);
			return builder;
		}
		
		public CreateTableBuilder key (DBKey key, String ... columns) {
			KEYS.add (Pair.mp (key, columns));
			return this;
		}
		
		@Override
		public DBQuery asQuery () {
			DBValidator.testName (name, this);
			DBValidator.testColumns (COLUMNS);
			
			StringBuilder sb = new StringBuilder ("CREATE ");
			if (isTemporary) {
				sb.append ("TEMPORARY ");
			}
			
			sb.append ("TABLE ");
			if (onlyOnAbset) {
				sb.append ("IF NOT EXISTS ");
			}
			
			sb.append ("'").append (name).append ("' ");
			if (like != null && like.length () > 0) {
				sb.append ("LIKE `").append (like).append ("`");
			} else {
				sb.append ("(");
				for (int i = 0; i < COLUMNS.size (); i++) {
					sb.append (COLUMNS.get (i).asQuery ());
					if (i < COLUMNS.size () - 1) {
						sb.append (", ");
					}
				}
				
				if (KEYS.size () > 0) {
					sb.append (", ");
				}
				for (int i = 0; i < KEYS.size (); i++) {
					Pair <DBKey, String []> key = KEYS.get (i);
					sb.append (key.F.name ()).append (" KEY (");
					StringJoiner joiner = new StringJoiner (", ");
					Arrays.asList (key.S).stream ()
						  .map (s -> "'" + s + "'")
						  .forEach (joiner::add);
					sb.append (joiner).append (")");
					if (i < COLUMNS.size () - 1) {
						sb.append (", ");
					}
				}
				sb.append (")");
			}
			
			return new DBQuery (sb.toString ());
		}
		
	}
	
	/* 
	 * ╔═════════════════════╗
	 * ║                     ║
	 * ║ SELECT BUILDER PART ║
	 * ║                     ║
	 * ╚═════════════════════╝
	 * 
	 * Documentation:
	 * https://phpclub.ru/mysql/doc/select.html
	 * 
	 */
	
	public static SelectBuilder buildSELECT () {
		return new SelectBuilder ();
	}
	
	public static class SelectBuilder extends DBQueryBuilder {
		
		private SelectBuilder () {}

		public SelectBuilder from () {
			return this;
		}
		
		public SelectBuilder straightJoin () {
			return this;
		}
		
		public SelectBuilder sqlSmallResult () {
			return this;
		}
		
		public SelectBuilder sqlBigResult () {
			return this;
		}
		
		public SelectBuilder sqlBufferResult () {
			return this;
		}
		
		public SelectBuilder sqlCache (boolean useCache) {
			return this;
		}
		
		public SelectBuilder filter (DBFilter filter) {
			return this;
		}
		
		// TODO: select expression is next
		
		@Override
		public DBQuery asQuery () {
			return null;
		}
		
	}
	
}
