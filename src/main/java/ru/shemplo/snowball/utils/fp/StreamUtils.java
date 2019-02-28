package ru.shemplo.snowball.utils.fp;

import static java.util.Spliterator.*;

import java.util.Iterator;
import java.util.Objects;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import ru.shemplo.snowball.utils.fp.AStream.TriFunction;

public interface StreamUtils {
    
    static <I, O> Stream <O> whilst (Predicate <I> condition, 
            Function <I, O> step, I obj) {
        Stream.Builder <O> builder = Stream.builder ();
        while (condition.test (obj)) {
            O tmp = step.apply (obj);
            builder.add (tmp);
        }
        
        return builder.build ();
    }
    
    static <I, O> Stream <O> dowhilst (Predicate <O> condition,
            Function <I, O> step, I obj) {
        Stream.Builder <O> builder = Stream.builder ();
        O tmp = null;
        do { 
            if (tmp != null) { builder.add (tmp); }
            tmp = step.apply (obj); 
        } while (tmp != null && condition.test (tmp));
        
        return builder.build ();
    }
    
    static <I extends Iterable <O>, O> Stream <O> takeIf (Predicate <O> condition,
            Function <I, O> step, I obj) {
        throw new UnsupportedOperationException ();
    }
    
    static <I1, I2, O> AStream <O, ?> zip (Stream <I1> left, Stream <I2> right, 
            BiFunction <I1, I2, O> zipper) {
        Objects.requireNonNull (zipper);
        Spliterator <? extends I1> aSpliterator = Objects.requireNonNull (left ).spliterator ();
        Spliterator <? extends I2> bSpliterator = Objects.requireNonNull (right).spliterator ();
        
        // Zipping looses DISTINCT and SORTED characteristics
        int characteristics = aSpliterator.characteristics () 
                            & bSpliterator.characteristics () 
                            & ~(DISTINCT | SORTED);
        
        long zipSize = ((characteristics & SIZED) != 0)
                    ? Math.min (aSpliterator.getExactSizeIfKnown (), 
                                bSpliterator.getExactSizeIfKnown ())
                    : -1;
        
        Iterator <I1> aIterator = Spliterators.iterator (aSpliterator);
        Iterator <I2> bIterator = Spliterators.iterator (bSpliterator);
        Iterator <O> cIterator = new Iterator <O> () {
            
            @Override
            public boolean hasNext () { 
                return aIterator.hasNext () && bIterator.hasNext (); 
            }
            
            @Override
            public O next () { 
                return zipper.apply (aIterator.next (), bIterator.next ()); 
            }
            
        };
        
        Spliterator <O> split = Spliterators.spliterator (cIterator, zipSize, characteristics);
        return AStreamProxy.make ((left.isParallel () || right.isParallel ()) 
                                  ? StreamSupport.stream (split, true)
                                  : StreamSupport.stream (split, false));
    }
    
    static <I1, I2, I3, O> AStream <O, ?> zip (Stream <I1> first, Stream <I2> second, 
            Stream <I3> third, TriFunction <I1, I2, I3, O> zipper) {
        Objects.requireNonNull (zipper);
        Spliterator <? extends I1> aSpliterator = Objects.requireNonNull (first ).spliterator ();
        Spliterator <? extends I2> bSpliterator = Objects.requireNonNull (second).spliterator ();
        Spliterator <? extends I3> cSpliterator = Objects.requireNonNull (third ).spliterator ();
        
        // Zipping looses DISTINCT and SORTED characteristics
        int characteristics = aSpliterator.characteristics () 
                            & bSpliterator.characteristics () 
                            & cSpliterator.characteristics () 
                            & ~(DISTINCT | SORTED);
        
        long zipSize = ((characteristics & SIZED) != 0)
                    ? Math.min (
                        Math.min (
                            aSpliterator.getExactSizeIfKnown (), 
                            bSpliterator.getExactSizeIfKnown ()
                          ),
                        cSpliterator.getExactSizeIfKnown ()
                      )
                    : -1;
        
        Iterator <I1> aIterator = Spliterators.iterator (aSpliterator);
        Iterator <I2> bIterator = Spliterators.iterator (bSpliterator);
        Iterator <I3> cIterator = Spliterators.iterator (cSpliterator);
        Iterator <O> dIterator = new Iterator <O> () {
            
            @Override
            public boolean hasNext () { 
                return aIterator.hasNext () && bIterator.hasNext (); 
            }
            
            @Override
            public O next () { 
                return zipper.apply (aIterator.next (), 
                 bIterator.next (), cIterator.next ()); 
            }
            
        };
        
        Spliterator <O> split = Spliterators.spliterator (dIterator, zipSize, characteristics);
        return AStreamProxy.make ((first.isParallel () || second.isParallel () || third.isParallel ()) 
                                  ? StreamSupport.stream (split, true) : StreamSupport.stream (split, false));
    }
    
}
