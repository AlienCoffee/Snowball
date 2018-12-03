package ru.shemplo.snowball.utils.db;

import java.util.*;

import ru.shemplo.snowball.stuctures.Pair;

@Deprecated
public abstract class DBQueryBuilder {
	
	/*
	 * create ().table ("products").columns (
	 *   column ("id", INT.len (11)).flags (AI | NN),
	 *   column ("name", TEXT).flags (NN),
	 *   column ("price", INT).flags (NN).default ("0")
	 * ).keys (
	 *   key (PRIMARY, "id")
	 * ).ifNotExists ().collate ("utf8_unicode_ci").engine (InnoDB)
	 * 
	 * 
	 * 
	 * select ().fields (
	 *   fnc (COUNT, val ("*")).as ("size"),
	 *   fld ("id"),
	 *   val ("some value")
	 *   // all ()
	 * ).from ("products").where (
	 *   eq (fnc (MOD, fld ("price"), 10), val ("0")), // and
	 *   more (fld ("id"), val ("0")), // and
	 *   or (eq (val ("1"), val ("2")), more (val ("1"), val ("2")))
	 * ).limit (1, 4)
	 * 
	 */
	
	
	
	
	
	private DBQueryBuilder () {}
	
	public abstract DBQuery asQuery ();
	
	//-------------------------------//
	
	//-------------------------------//
	
	/* 
	 * ╔════════════════════════════╗
	 * ║ CREATE COLUMN BUILDER PART ║
	 * ╚════════════════════════════╝
	 */
	
	public static class ColumnBuilder <Owner extends DBQueryBuilder> extends DBQueryBuilder {
		
		private final Owner OWNER;
		
		private boolean isNotNull = false, 
						autoIncrement = false;
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
		
		public ColumnBuilder <Owner> ai () {
			this.autoIncrement = true;
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
			if (autoIncrement) {
				sb.append (" AUTOINCREMENT");
			}
			if (isNotNull) {
				sb.append (" NOT NULL");
			}
			
			return new DBQuery (sb.toString ());
		}
		
	}
	
	/* 
	 * ╔═══════════════════════════╗
	 * ║ SELECT FIELD BUILDER PART ║
	 * ╚═══════════════════════════╝
	 * 
	 */
	
	public static class SelectFieldBuilder <Owner extends DBQueryBuilder> extends DBQueryBuilder {

		private String query = "", as = "";
		private final Owner OWNER;
		
		public SelectFieldBuilder (Owner owner) {
			this.OWNER = owner;
		}
		
		public SelectFieldBuilder <Owner> column (String name) {
			this.query = "`" + name + "`";
			return this;
		}
		
		public SelectFieldBuilder <Owner> function (DBFunction function, String ... args) {
			DBValidator.testFunctionArguments (function, args);
			return function (function.name (), args);
		}
		
		public SelectFieldBuilder <Owner> function (String function, String ... args) {
			StringJoiner joiner = new StringJoiner (", ");
			if (args != null) {
				Arrays.asList (args).forEach (joiner::add);
			}
			
			StringBuilder sb = new StringBuilder (function);
			sb.append ("(").append (joiner.toString ()).append (")");
			this.query = sb.toString ();
			
			return this;
		}
		
		public SelectFieldBuilder <Owner> expression (String expression) {
			this.query = expression;
			return this;
		}
		
		public SelectFieldBuilder <Owner> as (String label) {
			this.as += "AS '" + label + "'";
			return this;
		}
		
		public SelectFieldBuilder <Owner> all () {
			this.query = "*";
			return this;
		}
		
		public Owner add () {
			return OWNER;
		}
		
		@Override
		public DBQuery asQuery () {
			if ("*".equals (query.trim ())) {
				String text = "Select `*` can't be used with AS";
				throw new IllegalStateException (text);
			}
			
			DBValidator.testName (as, this);
			return new DBQuery (query + " " + as);
		}
		
	}
	
	/* 
	 * ╔══════════════════════════╗
	 * ║ SELECT INTO BUILDER PART ║
	 * ╚══════════════════════════╝
	 * 
	 */
	
	public static class SelectIntoBuilder <Owner extends DBQueryBuilder> extends DBQueryBuilder {

		private boolean useOutfile = true;
		private String filename = "";
		
		private final Owner OWNER;
		
		public SelectIntoBuilder (Owner owner) {
			this.OWNER = owner;
		}
		
		public SelectIntoBuilder <Owner> outfile () {
			this.useOutfile = true;
			return this;
		}
		
		public SelectIntoBuilder <Owner> dumpfile () {
			this.useOutfile = false;
			return this;
		}
		
		public SelectIntoBuilder <Owner> file (String filename) {
			this.filename = filename;
			return this;
		}
		
		public Owner done () {
			return OWNER;
		}
		
		@Override
		public DBQuery asQuery () {
			DBValidator.testName (filename, this);
			
			StringBuilder sb = new StringBuilder ("INTO ");
			if (useOutfile) {
				sb.append ("OUTFILE ");
			} else {
				sb.append ("DUMPFILE ");
			}
			
			sb.append ("'").append (filename).append ("'");
			return new DBQuery (sb.toString ());
		}
		
	}
	
	/* 
	 * ╔════════════════════╗
	 * ║ WHERE BUILDER PART ║
	 * ╚════════════════════╝
	 * 
	 */
	
	public static class WhereBuilder <Owner extends DBQueryBuilder> extends DBQueryBuilder {

		private final List <String> EXPRS = new ArrayList <> ();
		private final Owner OWNER;
		
		public WhereBuilder (Owner owner, String expression) {
			this.EXPRS.add ("(" + expression + ")");
			this.OWNER = owner;
		}
		
		public WhereBuilder <Owner> and (String ... ors) {
			if (ors == null || ors.length == 0) {
				return this;
			}
			
			StringJoiner joiner = new StringJoiner (" OR ");
			Arrays.asList (ors).forEach (joiner::add);
			
			EXPRS.add ("(" + joiner.toString () + ")");
			return this;
		}
		
		public Owner done () {
			return OWNER;
		}
		
		@Override
		public DBQuery asQuery () {
			for (String expression : EXPRS) {
				DBValidator.testName (expression, this);
			}
			
			StringBuilder sb = new StringBuilder ("WHERE ");
			
			StringJoiner joiner = new StringJoiner (" AND ");
			EXPRS.forEach (joiner::add);
			
			sb.append (joiner);
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
		private SelectIntoBuilder <SelectBuilder> into = null;
		private WhereBuilder <SelectBuilder> where = null;
		private DBFilter filter = null;
		private String table = "";
		
		private enum LimType {
			NONE, SINGLE, DOUBLE;
		}
		private LimType limitType = LimType.NONE;
		private int limA = 0, limB = 0;
		
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
			SelectFieldBuilder <SelectBuilder> 
				builder = new SelectFieldBuilder <> (this);
			
			SELECT.add (builder);
			return builder;
		}
		
		public SelectIntoBuilder <SelectBuilder> into () {
			this.into = new SelectIntoBuilder <> (this);
			return into;
		}
		
		public SelectBuilder from (String table) {
			this.table = "`" + table + "`";
			return this;
		}
		
		public WhereBuilder <SelectBuilder> where (String expression) {
			this.where = new WhereBuilder <> (this, expression);
			return where;
		}
		
		public SelectBuilder limit (int limit) {
			this.limitType = LimType.SINGLE;
			this.limA = limit;
			return this;
		}
		
		public SelectBuilder limit (int limit, int offset) {
			this.limitType = LimType.DOUBLE;
			this.limB = offset;
			this.limA = limit;
			
			return this;
		}
		
		@Override
		public DBQuery asQuery () {
			DBValidator.testName (table, this);
			
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
			
			StringJoiner joiner = new StringJoiner (", ");
			SELECT.forEach (f -> joiner.add (f.asQuery ().toString ()));
			sb.append (joiner.toString ()).append (" ");
			
			if (into != null) {
				sb.append (into.asQuery ().toString ()).append (" ");
			}
			
			sb.append ("FROM ").append (table);
			
			if (where != null) {
				sb.append (" ").append (where.asQuery ().toString ());
			}
			
			if (!LimType.NONE.equals (limitType)) {
				sb.append (" LIMIT ").append (limA);
				
				if (LimType.DOUBLE.equals (limitType)) {
					sb.append (", ").append (limB);
				}
			}
			
			return new DBQuery (sb.toString ());
		}
		
	}
	
}
