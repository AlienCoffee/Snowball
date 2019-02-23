package ru.shemplo.snowball.stuctures;

import java.io.Serializable;

import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.Function;

import ru.shemplo.snowball.utils.MurmurHash;

public class Pair <F, S> implements Serializable {
    
    private static final long serialVersionUID = -1422594531249230932L;
    
    public final F F;
    public final S S;
    
    public Pair (F F, S S) {
        this.F = F; this.S = S;
    }
    
    @Override
    public String toString () {
        return "<" + F + "; " + S + ">";
    }
    
    @Override
    public boolean equals (Object obj) {
        if (Objects.isNull (obj) 
            || !(obj instanceof Pair)) { 
            return false; 
        }
        
        if (obj == this) {
        	return true;
        }
        
        Pair <?, ?> pair = (Pair <?, ?>) obj;
        return (F == null ? pair.F == null : F.equals (pair.F))
            && (S == null ? pair.S == null : S.equals (pair.S));
    }
    
    public F getF () { return F; }
    public S getS () { return S; }
    
    public <R> Pair <R, S> applyF (Function <F, R> converter) {
        return mp (converter.apply (getF ()), getS ());
    }
    
    public <R> Pair <F, R> applyS (Function <S, R> converter) {
        return mp (getF (), converter.apply (S));
    }
    
    @Override
    public int hashCode () {
    	return new MurmurHash ()
    		 . update (F)
    		 . update (S)
    		 . finish (2);
    }
    
    public Pair <S, F> swap () {
    	return Pair.mp (S, F);
    }
    
    public boolean same () {
        @SuppressWarnings ("unlikely-arg-type")
        boolean result = Objects.equals (F, S);
        
        return result;
    }
    
    public static <F, S> Pair <F, S> mp (F F, S S) {
        return new Pair <> (F, S);
    }
    
    public static <T> Pair <T, T> dup (T T) {
        return mp (T, T);
    }
    
    public static <F, S> Pair <F, S> fromMapEntry (Entry <F, S> entry) {
        return mp (entry.getKey (), entry.getValue ());
    }
    
}
