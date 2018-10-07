package ru.shemplo.dsau.stuctures;

import java.util.HashSet;
import java.util.Set;

public class OwnedVariable <T> {

	private final Set <Object> owners;
	private T value;
	
	public OwnedVariable (Object... owners) {
	    this.owners = new HashSet <> ();
	    for (Object object : owners) {
	    	if (object != null) {
	    		this.owners.add (object);
	    	}
	    }
	}
	
	public T read () {
	    return value;
	}
	
	public T readNotNull (T defaultValue) {
	    return (value == null) ? defaultValue : value;
	}
	
	public void write (T value, Object owner) {
	    if (this.owners.contains (owner)) {
			this.value = value;
	    }
	}
	
}
