package ru.shemplo.snowball.utils.db;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
	
	public static class SelectFieldBuilder <Owner extends DBQueryBuilder> extends DBQueryBuilder {

		private final Owner OWNER;
		
		public SelectFieldBuilder (Owner owner) {
			this.OWNER = owner;
		}
		
		public SelectFieldBuilder <Owner> column (String name) {
			return this;
		}
		
		public SelectFieldBuilder <Owner> function (DBFunction function, String ... args) {
			DBValidator.testFunctionArguments (function, args);
			return function (function.name (), args);
		}
		
		public SelectFieldBuilder <Owner> function (String function, String ... args) {
			return this;
		}
		
		public SelectFieldBuilder <Owner> expression (String expression) {
			return this;
		}
		
		public SelectFieldBuilder <Owner> as (String label) {
			return this;
		}
		
		public Owner add () {
			return OWNER;
		}
		
		@Override
		public DBQuery asQuery () {
			return null;
		}
		
	}
	
	public static class SelectIntoBuilder <Owner extends DBQueryBuilder> extends DBQueryBuilder {

		private final Owner OWNER;
		
		public SelectIntoBuilder (Owner owner) {
			this.OWNER = owner;
		}
		
		public Owner done () {
			return OWNER;
		}
		
		@Override
		public DBQuery asQuery () {
			return null;
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
		private final List <Pair <DBKey, Set <String>>>
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
		
		public CreateTableBuilder ifNotExists () {
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
			List <String> cols = Arrays.asList (columns);
			for (int i = 0; i < KEYS.size (); i++) {
				if (KEYS.get (i).F.equals (key)) {
					KEYS.get (i).S.addAll (cols);
					return this;
				}
			}
			
			KEYS.add (Pair.mp (key, new HashSet <> (cols)));
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
				if (COLUMNS.size () > 0) {
					System.err.println ("Columns was ignored in " + this);
				}
				
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
					Pair <DBKey, Set <String>> key = KEYS.get (i);
					sb.append (key.F.name ()).append (" KEY (");
					StringJoiner joiner = new StringJoiner (", ");
					key.S.stream ().map (s -> "'" + s + "'")
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
		
		private boolean isStraightJoin = false, isSqlSmallResult  = false,
						isSqlBigResult = false, isSqlBufferResult = false,
						isCachePolicy  = false, useCache          = false,
						calcFoundRows  = false, isHighPriority    = false;
		private DBFilter filter = null;
		
		private List <SelectFieldBuilder <SelectBuilder>> 
			SELECT = new ArrayList <> ();
		
		private SelectBuilder () {}
		
		public SelectBuilder straightJoin () {
			this.isStraightJoin = true;
			return this;
		}
		
		public SelectBuilder sqlSmallResult () {
			this.isSqlSmallResult = true;
			return this;
		}
		
		public SelectBuilder sqlBigResult () {
			this.isSqlBigResult = true;
			return this;
		}
		
		public SelectBuilder sqlBufferResult () {
			this.isSqlBufferResult = true;
			return this;
		}
		
		public SelectBuilder sqlCache (boolean useCache) {
			this.isCachePolicy = true;
			this.useCache = useCache;
			return this;
		}
		
		public SelectBuilder sqlCalc () {
			this.calcFoundRows = true;
			return this;
		}
		
		public SelectBuilder priority () {
			this.isHighPriority = true;
			return this;
		}
		
		public SelectBuilder filter (DBFilter filter) {
			this.filter = filter;
			return this;
		}
		
		public SelectFieldBuilder <SelectBuilder> select () {
			return new SelectFieldBuilder <> (this);
		}
		
		public SelectIntoBuilder <SelectBuilder> into () {
			return new SelectIntoBuilder <> (this);
		}
		
		public SelectBuilder from () {
			return this;
		}
		
		@Override
		public DBQuery asQuery () {
			StringBuilder sb = new StringBuilder ("SELECT ");
			if (isStraightJoin) {
				sb.append ("STRAIGHT_JOIN ");
			}
			if (isSqlSmallResult) {
				sb.append ("SQL_SMALL_RESULT ");
			}
			if (isSqlBigResult) {
				sb.append ("SQL_BIG_RESULT ");
			}
			if (isSqlBufferResult) {
				sb.append ("SQL_BUFFER_RESULT ");
			}
			if (isCachePolicy) {
				if (useCache) {
					sb.append ("SQL_CACHE ");
				} else {
					sb.append ("SQL_NO_CACHE ");
				}
			}
			if (calcFoundRows) {
				sb.append ("SQL_CALC_FOUND_ROWS ");
			}
			if (isHighPriority) {
				sb.append ("HIGH_PRIORITY ");
			}
			if (filter != null) {
				sb.append (filter.name () + " ");
			}
			
			return new DBQuery (sb.toString ());
		}
		
	}
	
}
