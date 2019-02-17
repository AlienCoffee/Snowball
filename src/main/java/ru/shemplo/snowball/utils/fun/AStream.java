package ru.shemplo.snowball.utils.fun;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import ru.shemplo.snowball.stuctures.Pair;

public interface AStream <T, MT> extends Stream <T> {
    
    /**
     * 
     * @param stream
     * 
     * @return
     * 
     */
    public <R> AStream <R, MT> from (Stream <R> stream);
    
    default <F extends Function <T, R>, R> AStream <R, MT> map2 (F f) {
        return from (this.map (f));
    }
    
    /**
     * 
     * @param f
     * @param arg
     * 
     * @return
     * 
     */
    default <I, R> AStream <R, MT> map (BiFunction <T, I, R> f, I arg) {
        return from (this.map (t -> f.apply (t, arg)));
    }
    
    /**
     * 
     * @author Shemplo
     *
     * @param <T1>
     * @param <T2>
     * @param <T3>
     * @param <R>
     * 
     */
    @FunctionalInterface
    public static interface TriFunction <T1, T2, T3, R> {
        public R apply (T1 arg1, T2 arg2, T3 arg3);
    }
    
    /**
     * 
     * @param f
     * @param arg1
     * @param arg2
     * 
     * @return
     * 
     */
    default <I1, I2, R> AStream <R, MT> map (TriFunction <T, I1, I2, R> f, I1 arg1, I2 arg2) {
        return from (this.map (t -> f.apply (t, arg1, arg2)));
    }
    
    /**
     * 
     * @author Shemplo
     *
     * @param <T1>
     * @param <T2>
     * @param <T3>
     * @param <T4>
     * @param <R>
     * 
     */
    @FunctionalInterface
    public static interface QuadFunction <T1, T2, T3, T4, R> {
        public R apply (T1 arg1, T2 arg2, T3 arg3, T4 arg4);
    }
    
    /**
     * 
     * @param f
     * @param arg1
     * @param arg2
     * @param arg3
     * 
     * @return
     * 
     */
    default <I1, I2, I3, R> AStream <R, MT> map (QuadFunction <T, I1, I2, I3, R> f, 
            final I1 arg1, final I2 arg2, final I3 arg3) {
        return from (this.map (t -> f.apply (t, arg1, arg2, arg3)));
    }
    
    /**
     * 
     * @param c
     * 
     */
    default void foreach (BiConsumer <Integer, T> c) {
        StreamUtils.zip (Stream.iterate (0, i -> i + 1), this, Pair::mp)
                   .forEach (p -> c.accept (p.getF (), p.getS ()));
    }
    
    /**
     * 
     * @param mapper
     * 
     * @return
     * 
     */
    public <R> AStream <R, T> memorize (Function <T, R> mapper);
    
    /**
     * 
     * @return
     * 
     */
    public AStream <MT, Void> recall ();
    
}
