package ru.shemplo.snowball.utils.fp;

import static java.util.Objects.*;

import java.util.Optional;
import java.util.function.Function;

import ru.shemplo.snowball.stuctures.Pair;

public class Conditional <L, R> extends Pair <L, R> {

    private static final long serialVersionUID = -3957814568897536490L;

    private Conditional (L left, R right) {
        super (left, right);
    }
    
    public L getLeft  () { return getF (); }
    public R getRight () { return getS (); }
    
    public boolean isLeft  () { return getF () != null; }
    public boolean isRight () { return getS () != null; }
    
    public <O> Optional <O> ifLeft (Function <L, O> function) {
        if (isLeft ()) {
            return Optional.ofNullable (
                requireNonNull (function)
                . apply (getLeft ())
            );
        }
        
        return Optional.empty ();
    }
    
    public L needLeft (L value) {
        if (isLeft ()) { return getLeft (); }
        return value;
    }
    
    public L needLeft (Function <R, L> converter) {
        if (isLeft ()) { return getLeft (); }
        
        return requireNonNull (converter)
             . apply (getRight ());
    }
    
    public L needLeft (Throwable throwable) throws Throwable {
        if (isLeft ()) { return getLeft (); }
        throw throwable;
    }
    
    public <T> Conditional <T, R> mapIfLeft (Function <L, T> mapper) {
        if (isLeft ()) { return left (requireNonNull (mapper).apply (getLeft ())); }
        return right (getRight ());
    }
    
    public <O> Optional <O> ifRight (Function <L, O> function) {
        if (isRight ()) {
            return Optional.ofNullable (
                requireNonNull (function)
                . apply (getLeft ())
            );
        }
        
        return Optional.empty ();
    }
    
    public R needRight (R value) {
        if (isRight ()) { return getRight (); }
        return value;
    }
    
    public R needRight (Function <L, R> converter) {
        if (isRight ()) { return getRight (); }
        
        return requireNonNull (converter)
             . apply (getLeft ());
    }
    
    public R needRight (Throwable throwable) throws Throwable {
        if (isRight ()) { return getRight (); }
        throw throwable;
    }
    
    public <T> Conditional <L, T> mapIfRight (Function <R, T> mapper) {
        if (isLeft ()) { return left (getLeft ()); }
        
        return right (
            requireNonNull (mapper)
            . apply (getRight ())
        );
    }
    
    public static <L, R> Conditional <L, R> tryLeft (L value, final R alternative) {        
        return new Conditional <> (value, requireNonNull (alternative));
    }
    
    public static <L, R> Conditional <L, R> left (L value) {
        return new Conditional <> (requireNonNull (value), null);
    }
    
    public static <L, R> Conditional <L, R> tryRight (R value, final L alternative) {
        return new Conditional <> (requireNonNull (alternative), value);
    }
    
    public static <L, R> Conditional <L, R> right (R value) {
        return new Conditional <> (null, requireNonNull (value));
    }
    
}
