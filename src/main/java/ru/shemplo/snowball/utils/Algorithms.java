package ru.shemplo.snowball.utils;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Queue;
import java.util.function.Function;

public class Algorithms {
 
    public static final <T> void runBFS (T start, Function <T, Boolean> onVisit, 
            Function <T, Collection <T>> children) {
        Queue <T> queue = new LinkedList <> ();
        queue.add (start);
        
        while (!queue.isEmpty ()) {
            T obj = queue.poll ();
            if (onVisit != null && onVisit.apply (obj)) {
                if (children != null) {                    
                    queue.addAll (children.apply (obj));
                }
            }
        }
    }
    
}
