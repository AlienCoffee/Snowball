package ru.shemplo.snowball.utils.fp;

import java.util.function.Function;
import java.util.function.Predicate;

import ru.shemplo.snowball.stuctures.Pair;

public class FunctionalUtils {
    
    public static class Case <T, R> extends Pair <Predicate <T>, Function <T, R>> {

        private static final long serialVersionUID = -5216911316412503411L;

        public Case (Predicate <T> F, Function <T, R> S) { super (F, S); }
        
        public static <T, R> Case <T, R> caseOf (Predicate <T> predicate, 
                Function <T, R> function) {
            return new Case <> (predicate, function);
        }
        
    };
    
    @SafeVarargs
    public static <T, R> R switch$ (T object, Case <T, R> ... cases) {
        R result = null;
        for (Case <T, R> $case : cases) {
            if ($case.F.test (object)) {
                result = $case.S.apply (object);
                break;
            }
        }
        
        return result;
    }
    
}
