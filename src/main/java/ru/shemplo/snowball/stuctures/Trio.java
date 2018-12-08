package ru.shemplo.snowball.stuctures;

import java.util.Objects;

import ru.shemplo.snowball.utils.MurmurHash;

public class Trio <F, S, T> {

	public final F F;
	public final S S;
    public final T T;
    
    public Trio (F F, S S, T T) {
        this.F = F; this.S = S;
        this.T = T;
    }
    
    @Override
    public String toString () {
        return "<" + F + "; " + S + "; " + T + ">";
    }
    
    @Override
    public boolean equals (Object obj) {
        if (Objects.isNull (obj) 
            || !(obj instanceof Trio)) { 
            return false; 
        }
        
        if (obj == this) {
        	return true;
        }
        
        Trio <?, ?, ?> trio = (Trio <?, ?, ?>) obj;
        return (F == null ? trio.F == null : F.equals (trio.F))
            && (S == null ? trio.S == null : S.equals (trio.S))
            && (T == null ? trio.T == null : T.equals (trio.T));
    }
    
    @Override
    public int hashCode () {
    	return new MurmurHash ()
    		 . update (F)
    		 . update (S)
    		 . update (T)
    		 . finish (3);
    }
    
    public static <F, S, T> Trio <F, S, T> mt (F F, S S, T T) {
        return new Trio <> (F, S, T);
    }
	
}
