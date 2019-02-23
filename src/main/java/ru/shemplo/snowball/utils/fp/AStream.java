package ru.shemplo.snowball.utils.fp;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import ru.shemplo.snowball.stuctures.Pair;

public interface AStream <T, MT> extends Stream <T> {
    
    /**
     * Converts native Java {@link Stream} to custom {@link AStream}.
     * 
     * @param stream which should be converted
     * 
     * @return instance of {@link AStream}
     * 
     */
    public <R> AStream <R, MT> from (Stream <R> stream);
    
    /**
     * 
     * @param f1 first function that will be applied
     * @param f2 second function that will be applied to intermediate result
     * 
     * @return stream with consistently applied map functions
     * 
     */
    default <Temp, R> AStream <R, MT> pipe (Function <T, Temp> f1, 
            Function <Temp, R> f2) {
        return from (map (f1).map (f2));
    }
    
    /**
     * 
     * @param f function that will be applied
     * @param arg additional argument to function (second one)
     * 
     * @return result of function application
     * 
     */
    default <I, R> AStream <R, MT> map (BiFunction <T, I, R> f, I arg) {
        return from (this.map (t -> f.apply (t, arg)));
    }
    
    /**
     * 
     * @author Shemplo
     *
     * @param <T1> type of the first argument
     * @param <T2> type of the second argument
     * @param <T3> type of the third argument
     * @param <R> return type
     * 
     */
    @FunctionalInterface
    public static interface TriFunction <T1, T2, T3, R> {
        public R apply (T1 arg1, T2 arg2, T3 arg3);
    }
    
    /**
     * 
     * @param f function that will be applied
     * @param arg1 additional argument to function (second one)
     * @param arg2 additional argument to function (third one)
     * 
     * @return
     * 
     */
    default <I1, I2, R> AStream <R, MT> map (TriFunction <T, I1, I2, R> f, I1 arg1, I2 arg2) {
        return from (map (t -> f.apply (t, arg1, arg2)));
    }
    
    /**
     * 
     * @author Shemplo
     *
     * @param <T1> type of the first argument
     * @param <T2> type of the second argument
     * @param <T3> type of the third argument
     * @param <T4> type of the fourth argument
     * @param <R> return type
     * 
     */
    @FunctionalInterface
    public static interface QuadFunction <T1, T2, T3, T4, R> {
        public R apply (T1 arg1, T2 arg2, T3 arg3, T4 arg4);
    }
    
    /**
     * 
     * @param f function that will be applied
     * @param arg1 additional argument to function (second one)
     * @param arg2 additional argument to function (third one)
     * @param arg3 additional argument to function (fourth one)
     * 
     * @return
     * 
     */
    default <I1, I2, I3, R> AStream <R, MT> map (QuadFunction <T, I1, I2, I3, R> f, 
            final I1 arg1, final I2 arg2, final I3 arg3) {
        return from (map (t -> f.apply (t, arg1, arg2, arg3)));
    }
    
    /**
     * 
     * @param c consumer for indexed stream entry
     * 
     */
    default void foreach (BiConsumer <Integer, T> c) {
        StreamUtils.zip (Stream.iterate (0, i -> i + 1), this, Pair::mp)
                   .forEach (p -> c.accept (p.getF (), p.getS ()));
    }
    
    /**
     * 
     * @param mapper function to split stream on two instances
     * 
     * @return {@link AStream} with memorized values
     * 
     */
    public <R> AStream <R, T> memorize (Function <T, R> mapper);
    
    /**
     * 
     * @return {@link Pair} with information if it has memorized values
     * 
     */
    public Pair <Boolean, AStream <T, MT>> hasMemorized ();
    
    /**
     * 
     * @return turn back to memorized values
     * 
     */
    public AStream <MT, Void> recall ();
    
    /**
     * 
     * @param stream which will be attached from the right
     * 
     * @return zipped streams
     * 
     */
    default <S> Stream <Pair <T, S>> zip (Stream <S> stream) {
        return StreamUtils.zip (this, stream, Pair::mp);
    }
    
    /**
     * 
     * @param stream stream which will be attached from the left
     * 
     * @return zipped streams
     * 
     */
    default <S> Stream <Pair <S, T>> zipL (Stream <S> stream) {
        return StreamUtils.zip (stream, this, Pair::mp);
    }
    
}
