package ru.shemplo.snowball.utils.fun;

import java.util.stream.Stream;

import java.util.function.Function;
import java.util.function.Predicate;

public interface StreamUtils {

    public static <I, O> Stream <O> whilst (Predicate <I> condition, 
            Function <I, O> step, I obj) {
        Stream.Builder <O> builder = Stream.builder ();
        while (condition.test (obj)) {
            O tmp = step.apply (obj);
            builder.add (tmp);
        }
        
        return builder.build ();
    }
    
}
