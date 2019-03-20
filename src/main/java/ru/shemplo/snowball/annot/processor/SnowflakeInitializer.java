package ru.shemplo.snowball.annot.processor;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.stream.Stream;

import ru.shemplo.snowball.annot.PostShaped;
import ru.shemplo.snowball.annot.Snowflake;

public final class SnowflakeInitializer <T> {
    
    private static final Predicate <AnnotatedElement> NEED_REFRESH
          = m -> !m.isAnnotationPresent (Snowflake.class) ? false
               : m.getAnnotation (Snowflake.class).refresh ();
    
    private final Class <? extends T> TOKEN;
    private final Method METHOD;
    private final Field FIELD;
    private T instance;
    
    private final boolean REFRESH;
    private final int PRIORITY;
    
    public SnowflakeInitializer (Class <? extends T> token, int priority) {
        this (token, null, null, null, priority, 
              NEED_REFRESH.test (token));
    }
    
    public SnowflakeInitializer (Field field, int priority) {
        this (null, field, null, null, priority, 
              NEED_REFRESH.test (field));
    }
    
    public SnowflakeInitializer (Method method, int priority) {
        this (null, null, method, null, priority, 
              NEED_REFRESH.test (method));
    }
    
    public SnowflakeInitializer (T instance, int priority) {
        this (null, null, null, instance, priority, false);
    }
    
    private SnowflakeInitializer (Class <? extends T> token, Field field, 
            Method method, T instance, int priority, boolean refresh) {
        this.TOKEN = token; this.FIELD = field; this.METHOD = method;
        this.instance = instance; this.PRIORITY = priority;
        this.REFRESH = refresh;
    }
    
    @Override
    public String toString () {
        return String.format ("SI of `%s` (t: %b, f: %b, m: %b, i: %b)", getType (), 
                    TOKEN != null, FIELD != null, METHOD != null, instance != null);
    }
    
    public Class <?> getType () {
        return TOKEN != null ? TOKEN
                      : (METHOD != null ? METHOD.getReturnType ()
                                 : (FIELD != null ? FIELD.getType ()
                                           : (instance != null ? instance.getClass ()
                                                        : null)));
    }
    
    public int getPriority () {
        return PRIORITY;
    }
    
    public boolean isRefreshing () {
        return REFRESH;
    }
    
    private Object [] context;
    
    public void rememberContext (Object ... args) {
        this.context = args;
    }
    
    public T getInstance () {
        return instance;
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
                
                result.add (type);
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
    
    public T init () {
        Objects.requireNonNull (context);
        return this.init (context);
    }
    
    public T init (SnowballContext context) {
        final T result = this.init ();
        initFields (context, result);
        return result;
    }
    
    public static void initFields (SnowballContext context, Object instance) {
        Arrays.asList (instance.getClass ().getDeclaredFields ()).forEach (f -> {
            try {
                final Class <?> type = f.getType ();
                f.setAccessible (true);
                
                if (Modifier.isStatic (f.getModifiers ())) { return; }
                if (Modifier.isFinal (f.getModifiers ())) { return; }
                if (type.isPrimitive ()) { return; }
                
                if (f.get (instance) != null) { return; } // already initialized
                
                if (f.isAnnotationPresent (Snowflake.class)) {
                    if (f.getAnnotation (Snowflake.class).manual ()) {
                        return; // field will be initialized manually
                    }
                }
                
                f.set (instance, context.getSnowflakeFor (type));
                if (context.registeredSnowflakes.get (type).isRefreshing ()) {
                    initFields (context, f.get (instance));
                }
            } catch (Exception e) { e.printStackTrace (); }
        });
        
        Arrays.asList (instance.getClass ().getDeclaredMethods ()).stream ()
        . filter (method -> method.isAnnotationPresent (PostShaped.class))
        . findFirst ().ifPresent (method -> {
            method.setAccessible (true);
            try   { method.invoke (instance, new Object [] {}); } 
            catch (IllegalAccessException | IllegalArgumentException 
                | InvocationTargetException e) {
                e.printStackTrace ();
            }
        });
    }
    
    public T init (Object ... args) {
        Objects.requireNonNull (args);
        
        if (instance != null && !REFRESH) { 
            return instance; 
        }
        
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
                
                return this.instance = constructor.newInstance (args);
            } else if (FIELD != null) {
                @SuppressWarnings ("unchecked")
                final T result = (T) FIELD.get (null);
                this.instance = result;
                
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
                this.instance = result;
                
                return result;
            }
        } catch (Exception e) { e.printStackTrace (); }
        
        return instance;
    }
    
}
