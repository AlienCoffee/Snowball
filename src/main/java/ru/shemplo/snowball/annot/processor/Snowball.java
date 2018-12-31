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
        
        final long start = System.currentTimeMillis ();
        
        Class <?> shapeClass = getCallingClass ();
        runWalkFrom (shapeClass.getPackage ());
        CONTEXT.addInitializer (shapeClass, 0);
        
        final long end = System.currentTimeMillis ();
        System.out.println (String.format ("Process is over (done by %dms)", 
                                           end - start));
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
            Snowflake snowflake = token.getAnnotation (Snowflake.class);
            final int priority = snowflake.priority ();
            CONTEXT.addInitializer (token, priority);
        }
        
        Arrays.asList (token.getDeclaredFields ())
        . forEach (Snowball::processField);
        
        Arrays.asList (token.getDeclaredMethods ())
        . forEach (Snowball::processMethod);
    }
    
    private static void processField (final Field field) {
        if (!Modifier.isPublic (field.getModifiers ()))   { return; }
        if (!Modifier.isStatic (field.getModifiers ()))   { return; }
        if (!Modifier.isFinal  (field.getModifiers ()))   { return; }
        if (!field.isAnnotationPresent (Snowflake.class)) { return; }
        if (field.getType ().isPrimitive ())              { return; }
        
        Snowflake snowflake = field.getAnnotation (Snowflake.class);
        final int priority = snowflake.priority ();
        CONTEXT.addInitializer (field, priority);
    }
    
    private static void processMethod (Method method) {
        if (!Modifier.isPublic (method.getModifiers ()))   { return; }
        if (!Modifier.isStatic (method.getModifiers ()))   { return; }
        if (!method.isAnnotationPresent (Snowflake.class)) { return; }
        if (method.getReturnType ().isPrimitive ())        { return; }
        
        Snowflake snowflake = method.getAnnotation (Snowflake.class);
        final int priority = snowflake.priority ();
        CONTEXT.addInitializer (method, priority);
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
    
    protected static void runBuildingHierarhy () {
        
    }
    
    protected void onShaped (String ... args) throws Exception {}
    
}
