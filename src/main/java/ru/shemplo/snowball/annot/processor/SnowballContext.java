package ru.shemplo.snowball.annot.processor;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;

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
        if (registeredPackages.contains (pkg)) {
            registeredPackages.add (pkg);
            unexploredPackages.add (pkg);
        }
    }
    
    final Set <Class <?>> needInjection
        = new HashSet <> ();
    final Set <Method> coolers
        = new HashSet <> ();
     
}
