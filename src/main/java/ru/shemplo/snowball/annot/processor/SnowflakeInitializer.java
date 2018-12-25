package ru.shemplo.snowball.annot.processor;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class SnowflakeInitializer {
    
    private final Class <?> TOKEN;
    private final Method METHOD;
    private final Field FIELD;
    
    public SnowflakeInitializer (Class <?> token) {
        this.TOKEN = token; this.FIELD = null;
        this.METHOD = null;
    }
    
    public SnowflakeInitializer (Field field) {
        this.TOKEN = null; this.FIELD = field;
        this.METHOD = null;
    }
    
    public SnowflakeInitializer (Method method) {
        this.TOKEN = null; this.FIELD = null;
        this.METHOD = method;
    }
    
    public List <Class <?>> getRequiredTokens (boolean deleteVarArgs) {
        List <Class <?>> result = new ArrayList <> ();
        if (TOKEN != null) {
            Arrays.asList (TOKEN.getDeclaredConstructors ()).stream ()
            . filter (c -> Modifier.isPublic (c.getModifiers ()))
            . filter (c -> {
                for (Class <?> type : c.getParameterTypes ()) {
                    if (type.isPrimitive ()) { return false; }
                }
                
                return true;
            })
            . sorted ((a, b) -> {
                int argsA = a.getParameterCount (), argsB = b.getParameterCount ();
                argsA -= a.isVarArgs () ? 1 : 0; argsB -= b.isVarArgs () ? 1 : 0;
                return Integer.compare (argsA, argsB);
            })
            . limit (1)
            . flatMap (c -> {
                Class <?> [] types = c.getParameterTypes ();
                if (c.isVarArgs () && deleteVarArgs) {
                    Class <?> [] tmp = new Class [types.length - 1];
                    System.arraycopy (types, 0, tmp, 0, tmp.length);
                    types = tmp; // for 1 argument less b/c it's varArgs
                }
                
                return Stream.of (types);
            })
            . forEach (result::add);
        } else if (METHOD != null) {
            
        }
        
        return result;
    }
    
    public List <Class <?>> getRequiredTokens () {
        return getRequiredTokens (true);
    }
    
    private boolean inputMatches (Object... input) {
        List <Class <?>> need = getRequiredTokens ();
        
        if (need.size () != input.length) { return false; }
        for (int i = 0; i < need.size (); i++) {
            if (input [i] == null) { continue; }
            
            final Class <?> token = need.get (i);
            if (!token.isAssignableFrom (input [i].getClass ())) {
                return false;
            }
        }
        
        return true;
    }
    
    public <R> R init (Object ... args) {
        Objects.requireNonNull (args);
        
        try {
            if (TOKEN != null && inputMatches (args)) {
                Class <?> [] types = getRequiredTokens (false)
                                   . toArray (new Class [0]);
                @SuppressWarnings ("unchecked")
                Constructor <R> constructor = (Constructor <R>) TOKEN
                                            . getConstructor (types);
                if (constructor.isVarArgs ()) {
                    Object [] tmp = new Object [args.length + 1];
                    if (args.length > 0) {
                        System.arraycopy (args, 0, tmp, 0, tmp.length);
                    }
                    
                    Class <?> stub = types [types.length - 1].getComponentType ();
                    tmp [args.length] = Array.newInstance (stub, 0);
                    args = tmp;
                }
                
                return constructor.newInstance (args);
            } else if (FIELD != null) {
                if (!Modifier.isPublic (FIELD.getModifiers ())) { return null; }
                if (!Modifier.isStatic (FIELD.getModifiers ())) { return null; }
                if (!Modifier.isFinal  (FIELD.getModifiers ())) { return null; }
                
                @SuppressWarnings ("unchecked")
                final R result = (R) FIELD.get (null);
                
                return result;
            } else if (METHOD != null && inputMatches (args)) {
                
            }
        } catch (Exception e) { e.printStackTrace (); }
        
        return null;
    }
    
}
