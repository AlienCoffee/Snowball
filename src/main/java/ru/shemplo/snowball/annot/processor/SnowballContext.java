package ru.shemplo.snowball.annot.processor;

import java.util.*;

public final class SnowballContext {
    
    SnowballContext () {}
    
    final Queue <Class <?>> unexploredClasses 
        = new LinkedList <> ();
    final Set <Class <?>> registeredClasses
        = new HashSet<> ();
    
    void addProjectClass (Class <?> token) {
        if (!registeredClasses.contains (token)) {            
            registeredClasses.add (token);
            unexploredClasses.add (token);
        }
    }
    
    final Queue <Package> unexploredPackages
        = new LinkedList <> ();
    final Set <Package> registeredPackages
        = new HashSet <> ();
    
    void addProjectPackage (Package pkg) {
        if (!registeredPackages.contains (pkg)) {
            registeredPackages.add (pkg);
            unexploredPackages.add (pkg);
        }
    }
    
    public final Map <Class <?>, SnowflakeInitializer> registeredSnowflakes
        = new HashMap <> ();
     
}
