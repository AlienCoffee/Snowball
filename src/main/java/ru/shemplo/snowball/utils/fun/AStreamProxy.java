package ru.shemplo.snowball.utils.fun;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Optional;
import java.util.Spliterator;
import java.util.function.*;
import java.util.stream.*;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import ru.shemplo.snowball.stuctures.Pair;


@RequiredArgsConstructor (access = AccessLevel.PRIVATE)
public class AStreamProxy <T, MT> implements AStream <T, MT> {

    /**
     * 
     */
    private final Stream <T> parent;
    
    /**
     * 
     */
    private final Stream <Pair <T, MT>> mparent;
    
    @Override
    public AStream <T, MT> filter (Predicate <? super T> predicate) {
        if (parent != null) { return make (parent.filter (predicate)); }
        
        Predicate <Pair <T, MT>> test = p -> predicate.test (p.getF ());
        return make (null, mparent.filter (test));
    }

    @Override
    public <R> AStream <R, MT> map (Function <? super T, ? extends R> mapper) {
        if (parent != null ) { return make (parent.map (mapper)); }
        
        @SuppressWarnings ("unchecked")
        Function <Pair <T, MT>, Pair <R, MT>> apply = 
            p -> p.applyF ((Function <T, R>) mapper);
        return make (null, mparent.map (apply));
    }

    @Override
    public IntStream mapToInt (ToIntFunction <? super T> mapper) {
        if (parent != null )return parent.mapToInt (mapper);
        return mparent.map (Pair::getF).mapToInt (mapper);
    }

    @Override
    public LongStream mapToLong (ToLongFunction <? super T> mapper) {
        if (parent != null )return parent.mapToLong (mapper);
        return mparent.map (Pair::getF).mapToLong (mapper);
    }

    @Override
    public DoubleStream mapToDouble (ToDoubleFunction <? super T> mapper) {
        if (parent != null )return parent.mapToDouble (mapper);
        return mparent.map (Pair::getF).mapToDouble (mapper);
    }

    @Override
    public <R> AStream <R, MT> flatMap (Function <? super T, ? extends Stream <? extends R>> mapper) {
        if (parent != null) return make (parent.flatMap (mapper));
        throw new UnsupportedOperationException ();
    }

    @Override
    public IntStream flatMapToInt (Function <? super T, ? extends IntStream> mapper) {
        if (parent != null) return parent.flatMapToInt (mapper);
        throw new UnsupportedOperationException ();
    }

    @Override
    public LongStream flatMapToLong (Function <? super T, ? extends LongStream> mapper) {
        if (parent != null) return parent.flatMapToLong (mapper);
        throw new UnsupportedOperationException ();
    }

    @Override
    public DoubleStream flatMapToDouble (Function <? super T, ? extends DoubleStream> mapper) {
        if (parent != null) return parent.flatMapToDouble (mapper);
        throw new UnsupportedOperationException ();
    }

    @Override
    public AStream <T, MT> distinct () {
        if (parent != null) return make (parent.distinct ());
        throw new UnsupportedOperationException ();
    }

    @Override
    public AStream <T, MT> sorted () {
        if (parent != null) return make (parent.sorted ());
        return make (null, mparent.sorted ());
    }

    @Override
    public AStream <T, MT> sorted (Comparator <? super T> comp) {
        if (parent != null) return make (parent.sorted (comp));
        
        final Comparator <Pair <T, MT>> comparator = 
            (a, b) -> comp.compare (a.getF (), b.getF ());
        return make (null, mparent.sorted (comparator));
    }

    @Override
    public AStream <T, MT> peek (Consumer <? super T> action) {
        if (parent != null) return make (parent.peek (action));
        return make (null, mparent.peek (p -> action.accept (p.getF ())));
    }

    @Override
    public AStream <T, MT> limit (long maxSize) {
        if (parent != null) return make (parent.limit (maxSize));
        return make (null, mparent.limit (maxSize));
    }

    @Override
    public AStream <T, MT> skip (long n) {
        if (parent != null) return make (parent.limit (n));
        return make (null, mparent.skip(n));
    }

    @Override
    public void forEach (Consumer <? super T> action) {
        if (parent != null) parent.forEach (action);
        mparent.forEach (p -> action.accept (p.getF ()));
    }

    @Override
    public void forEachOrdered (Consumer <? super T> action) {
        if (parent != null) parent.forEachOrdered (action);
        mparent.forEachOrdered (p -> action.accept (p.getF ()));
    }

    @Override
    public Object [] toArray () {
        if (parent != null) return parent.toArray ();
        return mparent.map (Pair::getF).toArray ();
    }

    @Override
    public <A> A [] toArray (IntFunction <A []> generator) {
        if (parent != null) return parent.toArray (generator);
        return mparent.map (Pair::getF).toArray (generator);
    }

    @Override
    public T reduce (T identity, BinaryOperator <T> accumulator) {
        if (parent != null) return parent.reduce (identity, accumulator);
        return mparent.map (Pair::getF).reduce (identity, accumulator);
    }

    @Override
    public Optional <T> reduce (BinaryOperator <T> accumulator) {
        if (parent != null) return parent.reduce (accumulator);
        return mparent.map (Pair::getF).reduce (accumulator);
    }

    @Override
    public <U> U reduce (U identity, BiFunction <U, ? super T, U> accumulator, 
            BinaryOperator <U> combiner) {
        if (parent != null) return parent.reduce (identity, accumulator, combiner);
        return mparent.map (Pair::getF).reduce (identity, accumulator, combiner);
    }

    @Override
    public <R> R collect (Supplier <R> supplier, BiConsumer <R, ? super T> accumulator, 
            BiConsumer <R, R> combiner) {
        if (parent != null) return parent.collect (supplier, accumulator, combiner);
        return mparent.map (Pair::getF).collect (supplier, accumulator, combiner);
    }

    @Override
    public <R, A> R collect (Collector <? super T, A, R> collector) {
        if (parent != null) return parent.collect (collector);
        return mparent.map (Pair::getF).collect (collector);
    }

    @Override
    public Optional <T> min (Comparator <? super T> comparator) {
        if (parent != null) return parent.min (comparator);
        return mparent.map (Pair::getF).min (comparator);
    }

    @Override
    public Optional <T> max (Comparator <? super T> comparator) {
        if (parent != null) return parent.max (comparator);
        return mparent.map (Pair::getF).max (comparator);
    }

    @Override
    public long count () {
        if (parent != null) return parent.count ();
        return mparent.count ();
    }

    @Override
    public boolean anyMatch (Predicate <? super T> predicate) {
        if (parent != null) return parent.anyMatch (predicate);
        return mparent.map (Pair::getF).anyMatch (predicate);
    }

    @Override
    public boolean allMatch (Predicate <? super T> predicate) {
        if (parent != null) return parent.allMatch (predicate);
        return mparent.map (Pair::getF).allMatch (predicate);
    }

    @Override
    public boolean noneMatch (Predicate <? super T> predicate) {
        if (parent != null) return parent.noneMatch (predicate);
        return mparent.map (Pair::getF).noneMatch (predicate);
    }

    @Override
    public Optional <T> findFirst () {
        if (parent != null) return parent.findFirst ();
        return mparent.map (Pair::getF).findFirst ();
    }

    @Override
    public Optional <T> findAny () {
        if (parent != null) return parent.findAny ();
        return mparent.map (Pair::getF).findAny ();
    }

    @Override
    public Iterator <T> iterator () {
        if (parent != null) return parent.iterator ();
        return mparent.map (Pair::getF).iterator ();
    }

    @Override
    public Spliterator <T> spliterator () {
        if (parent != null) return parent.spliterator ();
        return mparent.map (Pair::getF).spliterator ();
    }

    @Override
    public boolean isParallel () {
        if (parent != null) return parent.isParallel ();
        return mparent.map (Pair::getF).isParallel ();
    }

    @Override
    public AStream <T, MT> sequential () {
        if (parent != null) return make (parent.sequential ());
        return make (null, mparent.sequential ());
    }

    @Override
    public AStream <T, MT> parallel () {
        if (parent != null) return make (parent.parallel ());
        return make (null, mparent.parallel ());
    }

    @Override
    public AStream <T, MT> unordered () {
        if (parent != null) return make (parent.unordered ());
        return make (null, mparent.unordered ());
    }

    @Override
    public AStream <T, MT> onClose (Runnable closeHandler) {
        if (parent != null) return make (parent.onClose (closeHandler));
        return make (null, mparent.onClose (closeHandler));
    }

    @Override
    public void close () {
        if (parent != null) parent.close ();
        mparent.close ();
    }

    @Override
    public <R> AStream <R, MT> from (Stream <R> stream) {
        return make (stream);
    }
    
    @Override
    public <R> AStream <R, T> memorize (Function <T, R> mapper) {
        return make (null, parent.map (Pair::dup).map (p -> p.applyF (mapper)));
    }
    
    @Override
    public AStream <MT, Void> recall () {
        return make (mparent.map (Pair::getS));
    }
    
    public static final <R, MT> AStream <R, MT> make (Stream <R> stream, 
            Stream <Pair <R, MT>> mstream) { // combined constructor
        if (stream == null && mstream == null) {
            String message = "Failed to make new stream from NULL";
            throw new IllegalArgumentException (message);
        }
        
        return new AStreamProxy <> (stream, mstream);
    }
    
    public static final <R, MT> AStream <R, MT> make (Stream <R> stream) {
        return new AStreamProxy <> (stream, null);
    }
    
}
