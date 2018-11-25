package ru.shemplo.snowball.annot.processor;

import static ru.shemplo.snowball.stuctures.Instance.*;
import static ru.shemplo.snowball.utils.ClasspathUtils.*;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import ru.shemplo.snowball.annot.Cooler;
import ru.shemplo.snowball.annot.Init;
import ru.shemplo.snowball.annot.Polar;
import ru.shemplo.snowball.annot.Snowflake;
import ru.shemplo.snowball.stuctures.Instance;
import ru.shemplo.snowball.stuctures.Pair;

public abstract class Snowball {
    
    protected static final String CLASS_NAME  = Snowball.class.getName (),
                                  METHOD_NAME = "shape";
    private static final SnowballStorage STORAGE = new SnowballStorage ();
    private static boolean isSnowballMade = false;
    
    private static enum AnnotationHandler {
        
        SNOWFLAKE (Snowflake.class, i -> { STORAGE.addSnowflake ((Type) i); }),
        COOLER    (Cooler.class,    i -> { STORAGE.addCooler ((Method) i);  }),
        POLAR     (Polar.class,     i -> {
            Polar polar = ((Class <?>) i).getAnnotation (Polar.class);
            Arrays.stream (polar.scan ())
                  .map (Class::getPackage).map (Arrays::asList)
                  .forEach (Snowball::runGrowthOfShapeTo);
        }),
        INIT      (Init.class,      i -> {
            final Field field = (Field) i;
            STORAGE.addField (field);
            
            if (STORAGE.isClassVisited (field.getType ())) { return; }
            final Class <?> newToken = field.getType ();
            STORAGE.addVisitedClass (newToken);
            
            final Package pkg = newToken.getPackage ();
            runGrowthOfShapeTo (Arrays.asList (pkg));
            
            //addPackagesTree (pkg);
        });
        
        private final Class <? extends Annotation> TOKEN;
        private final Consumer <Object> CONSUMER;
        
        private AnnotationHandler (
                Class <? extends Annotation> annotationToken, 
                Consumer <Object> onVisit) {
            this.TOKEN = annotationToken;
            this.CONSUMER = onVisit;
        }
        
        public Class <? extends Annotation> getToken () {
            return TOKEN;
        }
        
        public <T> void accept (T object) {
            CONSUMER.accept (object);
        }
        
        public static AnnotationHandler getHandler (
                Class <? extends Annotation> annotation) {
            for (AnnotationHandler handler : values ()) {
                if (Objects.equals (handler.TOKEN, annotation)) {
                    return handler;
                }
            }
            
            return null;
        }
        
    }
    
    private static Set <Class <? extends Annotation>> FOLLOW_ANNOTATIONS = new HashSet <> (
        Arrays.asList (AnnotationHandler.values ()).stream ()
              .map (AnnotationHandler::getToken)
              .collect (Collectors.toList ())
    );
    
    private static final Map <Class <?>, Pair <Integer, Instance <?>>> 
        CONTEXT = new HashMap <> ();
    
    protected static final synchronized void shape (String ... args) {
        if (isSnowballMade) {
            String message = "Snowball is already successully made";
            throw new IllegalStateException (message);
        }
        
        CONTEXT.clear ();
        
        Snowball snowballInstance = createMainInstance (getInitClassName ());
        final Class <?> snowballToken = snowballInstance.getClass ();
        STORAGE.addInstance (snowballToken, valueOf (snowballInstance));
        STORAGE.addSnowflake (snowballToken);
        final List <Package> packages = Arrays.asList (
            snowballInstance.getClass ().getPackage (),
            Snowball.class.getPackage ()
        );
        
        Snowball.runGrowthOfShapeTo (packages);
        STORAGE.getCoolersWithTypes ().forEach (p -> {
            final Method method = p.F;
            
            try {
                Object tmp = method.invoke (Snowball.class);
                if (tmp == null) { return; }
                
                final Instance <?> result = Instance.valueOf (tmp);
                p.S.forEach (c -> STORAGE.addInstance (c, result));
            } catch (IllegalAccessException 
                  | IllegalArgumentException 
                  | InvocationTargetException es) {
                es.printStackTrace ();
            }            
        });
        
        STORAGE.getFields ().forEach (f -> {
            f.setAccessible (true);
            
            final Class <?> token       = f.getType (), 
                            parentToken = f.getDeclaringClass ();
            final Instance <?> parent = STORAGE.getInstance (parentToken);
            if (parent == null) {
                System.out.println (f.getName () + " " + token);
                String message = "Init field declared not in Snowflake";
                throw new IllegalStateException (message);
            }
            
            final Instance <?> instance = STORAGE.getInstance (token);
            if (instance == null) {
                String message = "Snowflake for `" + token + "` not found";
                throw new IllegalStateException (message);
            }
            
            try {
                f.set (parent.get (), instance.get ());
            } catch (IllegalArgumentException 
                  | IllegalAccessException es) {
                System.err.println (es.toString ());
            }
        });
        
        isSnowballMade = true;
        try   { snowballInstance.onShaped (); } 
        catch (Exception e) { e.printStackTrace (); }
    }

    private static final String getInitClassName () {
        StackTraceElement [] trace = Thread.currentThread()
                                   . getStackTrace();
       String initClassName = null; boolean take = false;
       for (StackTraceElement ste : trace) {
           if (take) {
               // Calling class found -> remember it
               initClassName = ste.getClassName ();
               break; // Nothing else matter
           }
       
           take =  Objects.equals (CLASS_NAME,  ste.getClassName ()) 
                && Objects.equals (METHOD_NAME, ste.getMethodName ());
       }
       
       if (initClassName == null) {
           String message = "Unable to determine Snowball class";
           throw new IllegalStateException (message);
       }
       
       return initClassName;
    }
    
    private static final Snowball createMainInstance (String initClassName) {
        try {
            ClassLoader cl = Thread.currentThread ().getContextClassLoader ();
            Class <?> token = Class.forName (initClassName, false, cl);
            if (Snowball.class.isAssignableFrom (token)) {
                @SuppressWarnings ("unchecked")
                Class <? extends Snowball> snowballToken = (Class <? extends Snowball>) token;
                return snowballToken.getConstructor ().newInstance ();
            } else {
                String message = String.format ("Type `%s` is not instacne of Snowball", token);
                throw new RuntimeException (message);
            }
        } catch (RuntimeException re) {
            throw re; // Just propagate
        } catch (Exception e) {
            throw new RuntimeException (e);
        }
    }
    
    private static final void runGrowthOfShapeTo (Collection <Package> packages) {
        findAllAnnotations (packages, FOLLOW_ANNOTATIONS).entrySet ()
            .stream ().map (e -> Pair.mp (e.getKey (), e.getValue ()))
            .forEach (e -> handleAnnotationEntry (e.F, e.S));
    }
    
    private static final <T> void handleAnnotationEntry (
            final Class <? extends Annotation> annotToken, 
            final List <T> entries) {
        AnnotationHandler handler = AnnotationHandler.getHandler (annotToken);
        if (handler == null) {
            String message = "Annotation handler not found for: " + annotToken;
            throw new IllegalStateException (message);
        }
        
        entries.forEach (handler::accept);
    }
    
    @SuppressWarnings ("unchecked")
    public static <R> R getSnowflakeFor (Class <?> token) {
        return (R) STORAGE.getInstance (token).get ();
    }
    
    public static void runOnInited (Runnable runnable) {
        throw new UnsupportedOperationException ();
    }
    
    protected void onShaped () throws Exception {}
    
}
