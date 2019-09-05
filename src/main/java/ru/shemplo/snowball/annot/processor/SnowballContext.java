package ru.shemplo.snowball.annot.processor;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public final class SnowballContext {
    
    SnowballContext () {}
    
    final Queue <Class <?>> unexploredClasses 
        = new LinkedList <> ();
    final Set <Class <?>> registeredClasses
        = new HashSet<> ();
    
    private String boundPackage = null;
    
    public void setBoundPackage (Package pkg) {
        boundPackage = pkg.getName ();
    }
    
    void addProjectClass (Class <?> token) {
        if (!registeredClasses.contains (token)) {
            registeredClasses.add (token);
            
            if ((boundPackage != null && token.getName ().startsWith (boundPackage)) 
                    || boundPackage == null) {
                unexploredClasses.add (token);
            }
        }
    }
    
    final Queue <Package> unexploredPackages
        = new LinkedList <> ();
    final Set <String> registeredPackages
        = new HashSet <> ();
    
    void addProjectPackage (Package pkg) {
        if (pkg == null) { return; }
        
        if (!registeredPackages.contains (pkg.getName ())) {
            registeredPackages.add (pkg.getName ());
            
            if ((boundPackage != null && pkg.getName ().startsWith (boundPackage)) 
                    || boundPackage == null) {
                unexploredPackages.add (pkg);
            }
        }
    }
    
    public final Map <Class <?>, SnowflakeInitializer <?>> 
        registeredSnowflakes = new HashMap <> ();
    
    void addInitializer (Class <?> token, SnowflakeInitializer <?> initializer) {
        final int priority = initializer.getPriority ();
        registeredSnowflakes.compute (token, 
            (k, v) -> (v == null || v.getPriority () < priority) 
                    ? initializer : v);
    }
    
    void addInitializer (Class <?> token, int priority) {
        addProjectPackage (token.getPackage ());
        addProjectClass (token);
        
        registeredSnowflakes.compute (token, 
            (k, v) -> (v == null || v.getPriority () < priority) 
                    ? new SnowflakeInitializer <> (token, priority) 
                    : v);
    }
    
    void addInitializer (Field field, int priority) {
        final Class <?> fieldType = field.getType ();
        registeredSnowflakes.compute (fieldType, 
            (k, v) -> (v == null || v.getPriority () < priority) 
                    ? new SnowflakeInitializer <> (field, priority) 
                    : v);
    }
    
    void addInitializer (Method method, int priority) {
        registeredSnowflakes.compute (method.getReturnType (), 
            (k, v) -> (v == null || v.getPriority () < priority) 
                    ? new SnowflakeInitializer <> (method, priority) 
                    : v);
    }
    
    @SuppressWarnings ("unchecked")
    public <R> R getSnowflakeFor (Class <?> type) {
        if (!registeredSnowflakes.containsKey (type)) { return null; }
        return (R) registeredSnowflakes.get (type).init ();
    }
    
}
