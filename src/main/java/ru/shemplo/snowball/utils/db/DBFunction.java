package ru.shemplo.snowball.utils.db;


public enum DBFunction {
	
	/*
	 * Documentation:
	 * https://phpclub.ru/mysql/doc/functions.html
	 * 
	 */
	
	CONCAT (-1), 
	COUNT  ( 1), 
	NOT    ( 1), 
	MAX    (-1), 
	MIN    (-1), 
	MOD    ( 2);
	
	public final int ARGN;
	
	private DBFunction (int arguments) {
		this.ARGN = arguments;
	}
	
}
