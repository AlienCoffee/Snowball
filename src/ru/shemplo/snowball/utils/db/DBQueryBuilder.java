package ru.shemplo.snowball.utils.db;

import java.util.ArrayList;
import java.util.List;

public abstract class DBQueryBuilder {
	
	private DBQueryBuilder () {}
	
	public abstract DBQuery asQuery ();
	
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
			sb.append ("`").append (name).append ("` ").append (type);
			if (isNotNull) {
				sb.append (" NOT NULL");
			}
			
			return new DBQuery (sb.toString ());
		}
		
	}
	
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
		
		private boolean onlyOnAbset = false;
		private String name;
		
		private CreateTableBuilder (String name) {
			this.name = name;
		}
		
		public CreateTableBuilder setName (String name) {
			this.name = name;
			return this;
		}
		
		public CreateTableBuilder onAbsent () {
			this.onlyOnAbset = true;
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
			return this;
		}
		
		@Override
		public DBQuery asQuery () {
			DBValidator.testName (name, this);
			
			StringBuilder sb = new StringBuilder ("CREATE TABLE ");
			if (onlyOnAbset) {
				sb.append (" IF NOT EXISTS ");
			}
			sb.append ("`").append (name).append ("`");
			
			sb.append ("(");
			for (int i = 0; i < COLUMNS.size (); i++) {
				sb.append (COLUMNS.get (i).asQuery ());
				if (i < COLUMNS.size () - 1) {
					sb.append (", ");
				}
			}
			sb.append (")");
			
			return new DBQuery (sb.toString ());
		}
		
	}
	
}
