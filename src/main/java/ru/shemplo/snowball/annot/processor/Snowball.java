package ru.shemplo.snowball.annot.processor;

import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Objects;

import ru.shemplo.snowball.annot.Snowflake;
import ru.shemplo.snowball.annot.Wind;
import ru.shemplo.snowball.utils.ClasspathUtils;

public abstract class Snowball {
    
    protected static final String CLASS_NAME  = Snowball.class.getName (),
                                  METHOD_NAME = "shape";
    protected static final SnowballContext CONTEXT = new SnowballContext ();
    private static boolean isSnowballShaped = false;
    
    protected static final synchronized void shape (String ... args) {
        if (isSnowballShaped) { return; }
        
        Class <?> shapeClass = getCallingClass ();
        runWalkFrom (shapeClass.getPackage ());
        
        System.out.println ("Process is over");
        isSnowballShaped = true;
    }
    
    protected static final synchronized void shape (Snowball instance, String ... args) {
        
    }
    
    protected static void runWalkFrom (Package pkg) {
        System.out.println (String.format ("Start walk from %s", pkg));
        CONTEXT.addProjectPackage (pkg);
        
        while (true) {
            boolean someTasksDone = false;
            while (!CONTEXT.unexploredClasses.isEmpty ()) {                
                Class <?> token = CONTEXT.unexploredClasses.poll ();
                CONTEXT.addProjectPackage (token.getPackage ());
                System.out.println (String.format ("%10s %s", "class", 
                                    token.getName ()));
                processClass (token);
                someTasksDone = true;
            }
            
            while (!CONTEXT.unexploredPackages.isEmpty ()) {
                System.out.println (String.format ("%10s %s", "package", 
                        CONTEXT.unexploredPackages.peek ().getName ()));
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
        if (token.isAnnotationPresent (Wind.class)) {
            Class <?> [] polars = token.getAnnotation (Wind.class).blow ();
            Arrays.asList (polars).stream ().map (Class::getPackage)
            . forEach (CONTEXT::addProjectPackage);
        }
        
        Class <?> container = token.getDeclaringClass ();
        if (token.isMemberClass () 
                && (!CONTEXT.registeredClasses.contains (container) 
                    || !Modifier.isStatic (token.getModifiers ()))) {
            System.out.println (String.format ("%10s %s", "", "unregistered"));
            CONTEXT.registeredClasses.remove (token);
            CONTEXT.unexploredClasses.remove (token);
            return;
        }
        
        if (token.isAnnotationPresent (Snowflake.class)) {
            CONTEXT.registeredSnowflakes.put (token, 
                  new SnowflakeInitializer (token));
        }
        
        Arrays.asList (token.getDeclaredFields ())
        . forEach (Snowball::processField);
        
        Arrays.asList (token.getDeclaredMethods ())
        . forEach (Snowball::processMethod);
        
        /*
        
        if (token.isAnnotationPresent (Snowflake.class)) {
            long registered = Arrays.asList (token.getDeclaredFields ()).stream ()
                            . filter (f -> !Modifier.isFinal (f.getModifiers ()))
                            . filter (f -> f.isAnnotationPresent (Polar.class))
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
        */
    }
    
    private static void processField (final Field field) {
        if (!Modifier.isPublic (field.getModifiers ())) { return; }
        if (!Modifier.isStatic (field.getModifiers ())) { return; }
        if (!Modifier.isFinal  (field.getModifiers ())) { return; }
        
        CONTEXT.registeredSnowflakes.put (field.getDeclaringClass (), 
                                   new SnowflakeInitializer (field));
    }
    
    private static void processMethod (Method method) {
        if (!Modifier.isPublic (method.getModifiers ())) { return; }
        if (!Modifier.isStatic (method.getModifiers ())) { return; }
        if (!Modifier.isFinal  (method.getModifiers ())) { return; }
        
        CONTEXT.registeredSnowflakes.put (method.getDeclaringClass (), 
                                   new SnowflakeInitializer (method));
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
