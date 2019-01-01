package ru.shemplo.snowball.annot.processor;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class SnowflakeInitializer <T> {
    
    private final Class <? extends T> TOKEN;
    private final Method METHOD;
    private final Field FIELD;
    private final T INSTANCE;
    
    private final int PRIORITY;
    
    public SnowflakeInitializer (Class <? extends T> token, int priority) {
        this (token, null, null, null, priority);
    }
    
    public SnowflakeInitializer (Field field, int priority) {
        this (null, field, null, null, priority);
    }
    
    public SnowflakeInitializer (Method method, int priority) {
        this (null, null, method, null, priority);
    }
    
    public SnowflakeInitializer (T instance, int priority) {
        this (null, null, null, instance, priority);
    }
    
    private SnowflakeInitializer (Class <? extends T> token, Field field, 
            Method method, T instance, int priority) {
        this.TOKEN = token; this.FIELD = field; this.METHOD = method;
        this.INSTANCE = instance; this.PRIORITY = priority;
    }
    
    @Override
    public String toString () {
        return super.toString ().concat (
            String.format (" (t: %b, f: %b, m: %b, i: %b)", TOKEN != null, 
                          FIELD != null, METHOD != null, INSTANCE != null)
        );
    }
    
    public int getPriority () {
        return PRIORITY;
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
            for (Class <?> type : METHOD.getParameterTypes ()) {
                if (type.isPrimitive ()) {
                    String message = String.format ("Snowflake method %s can't have "
                                         + "primitive arguments", METHOD.getName ());
                    throw new IllegalStateException (message);
                }
            }
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
    
    public T init (Object ... args) {
        Objects.requireNonNull (args);
        
        try {
            if (TOKEN != null && inputMatches (args)) {
                Class <?> [] types = getRequiredTokens (false)
                                   . toArray (new Class [0]);
                @SuppressWarnings ("unchecked")
                Constructor <T> constructor = (Constructor <T>) TOKEN
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
                @SuppressWarnings ("unchecked")
                final T result = (T) FIELD.get (null);
                
                return result;
            } else if (METHOD != null && inputMatches (args)) {
                Class <?> [] types = getRequiredTokens (false)
                                   . toArray (new Class [0]);
                if (METHOD.isVarArgs ()) {
                    Object [] tmp = new Object [args.length + 1];
                    if (args.length > 0) {
                        System.arraycopy (args, 0, tmp, 0, tmp.length);
                    }
                    
                    Class <?> stub = types [types.length - 1].getComponentType ();
                    tmp [args.length] = Array.newInstance (stub, 0);
                    args = tmp;
                }
                
                @SuppressWarnings ("unchecked")
                final T result = (T) METHOD.invoke (null, args);
                
                return result;
            }
        } catch (Exception e) { e.printStackTrace (); }
        
        return INSTANCE;
    }
    
}
