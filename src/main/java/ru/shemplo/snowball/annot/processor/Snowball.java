package ru.shemplo.snowball.annot.processor;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;

import ru.shemplo.snowball.annot.Cooler;
import ru.shemplo.snowball.annot.Init;
import ru.shemplo.snowball.annot.Polar;
import ru.shemplo.snowball.annot.Snowflake;
import ru.shemplo.snowball.utils.ClasspathUtils;

public abstract class Snowball {
    
    protected static final String CLASS_NAME  = Snowball.class.getName (),
                                  METHOD_NAME = "shape";
    private static final SnowballContext CONTEXT = new SnowballContext ();
    private static boolean isSnowballShaped = false;
    
    protected static final synchronized void shape (String ... args) {
        if (isSnowballShaped) { return; }
        
        Class <?> shapeClass = getCallingClass ();
        runWalkFrom (shapeClass.getPackage ());
        
        System.out.println ("Process is over");
        isSnowballShaped = true;
    }
    
    protected static void runWalkFrom (Package pkg) {
        while (true) {
            boolean someTasksDone = false;
            while (!CONTEXT.unexploredClasses.isEmpty ()) {                
                Class <?> token = CONTEXT.unexploredClasses.poll ();
                CONTEXT.addProjectPackage (token.getPackage ());
                processClass (token);
                someTasksDone = true;
            }
            
            while (!CONTEXT.unexploredPackages.isEmpty ()) {
                processPackage (CONTEXT.unexploredPackages.poll ());
                someTasksDone = true;                
            }
            
            if (!someTasksDone) { break; }
        }
    }

    private static final Class <?> getCallingClass () {
        StackTraceElement [] trace = Thread.currentThread ()
                                   . getStackTrace ();
        String callingClassName = null; boolean take = false;
        for (StackTraceElement ste : trace) {
            if (take) {
                // Calling class found -> remember it
                callingClassName = ste.getClassName ();
                break; // Nothing else matter
            }
        
            take  = Objects.equals (CLASS_NAME,  ste.getClassName  ()) 
                 && Objects.equals (METHOD_NAME, ste.getMethodName ());
        }
       
        if (callingClassName == null) {
            String message = "Unable to determine Snowball class";
            throw new IllegalStateException (message);
        }
        
        try {
            ClassLoader cl = Thread.currentThread  ()
                           . getContextClassLoader ();
            return Class.forName (callingClassName, false, cl);           
        } catch (ClassNotFoundException cnfe) { /* impossible */ }
        
        return null;
    }
    
    private static void processClass (Class <?> token) {
        if (token.isAnnotationPresent (Polar.class)) {
            Class <?> [] polars = token.getAnnotation (Polar.class)
                                . scan ();
            Arrays.asList (polars).stream ()
            . map     (Class::getPackage)
            . forEach (CONTEXT::addProjectPackage);
        }
        
        if (token.isAnnotationPresent (Snowflake.class)) {
            long registered = Arrays.asList (token.getDeclaredFields ()).stream ()
                            . filter (f -> !Modifier.isFinal (f.getModifiers ()))
                            . filter (f -> f.isAnnotationPresent (Init.class))
                            . map    (Field::getType)
                            . peek   (CONTEXT::addProjectClass)
                            . count  ();
            if (registered > 0) { CONTEXT.needInjection.add (token); }
        }
        
        Arrays.asList (token.getDeclaredMethods ()).stream ()
        . filter  (m -> !Modifier.isAbstract (m.getModifiers ()))
        . filter  (m -> Modifier.isStatic (m.getModifiers ()))
        . filter  (m -> Modifier.isPublic (m.getModifiers ()))
        . filter  (m -> m.isAnnotationPresent (Cooler.class))
        . forEach (CONTEXT.coolers::add);
    }
    
    private static void processPackage (Package pkg) {
        ClasspathUtils.getPackagesInTree (pkg)
        . forEach (CONTEXT::addProjectPackage);
        
        try {
            ClasspathUtils.getClassesFromPackage (pkg)
            . forEach (CONTEXT::addProjectClass);
        } catch (IOException ioe) { 
            throw new RuntimeException (ioe);
        }
    }
    
    @SuppressWarnings ("unused")
    private static final Snowball createMainInstance (String className) {
        try {
            ClassLoader cl = Thread.currentThread ().getContextClassLoader ();
            Class <?> token = Class.forName (className, false, cl);
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
    
    protected void onShaped (String ... args) throws Exception {}
    
}
