package ru.shemplo.snowball.utils.fun;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class PoolOfHandlers <T> {
    
    public static <T> Builder <T> build () {
        return new Builder <> ();
    }
    
    public static class Builder <T> {
    
        private volatile boolean isBuilt = false;
        private final PoolOfHandlers <T> pool;
        
        public Builder () {
            this.pool = new PoolOfHandlers <> ();
        }
        
        public Builder <T> addConsumer (BiConsumer <T, Object> consumer) {
            checkBuilderState ();
            pool.handlers2.add (consumer);
            return this;
        }
        
        public Builder <T> addConsumer (Consumer <T> consumer) {
            checkBuilderState ();
            pool.handlers1.add (consumer);
            return this;
        }
        
        private final void checkBuilderState () {
            if (isBuilt) { throw new IllegalStateException ("Object is built"); }            
        }
        
        public PoolOfHandlers <T> done () {
            this.isBuilt = true;
            return pool;
        }
        
    }
    
    private final List <BiConsumer <T, Object>> handlers2 = new ArrayList <> ();
    private final List <Consumer <T>> handlers1 = new ArrayList <> ();
    
    private PoolOfHandlers () {}
    
    public <S> void handle (T input, S support) {
        handlers2.forEach (h -> h.accept (input, support));
        handlers1.forEach (h -> h.accept (input));
    }
    
}
