package ru.shemplo.snowball.utils;

import java.io.IOException;
import java.lang.annotation.Annotation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileManager.Location;
import javax.tools.JavaFileObject;
import javax.tools.JavaFileObject.Kind;
import javax.tools.StandardLocation;
import javax.tools.ToolProvider;

public class ClasspathUtils {
    
    /**
     * ...
     * 
     * @require JDK (not JRE)
     * 
     * @param pkg
     * 
     * @return
     * 
     */
    public static final List <Class <?>> getClassesFromPackage (Package pkg) throws IOException {
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler ();
        if (compiler == null) {
            String message = "This method requires to be runned under JDK";
            throw new IllegalStateException (message);
        }
        
        JavaFileManager manager = compiler.getStandardFileManager (null, null, null);
        final Location location = StandardLocation.CLASS_PATH;
        String packageName = pkg.getName ();
        
        ClassLoader cl = Thread.currentThread ().getContextClassLoader ();
        Set <Kind> kinds = new HashSet <> (); kinds.add (Kind.CLASS);
        Iterable <JavaFileObject> objects = manager.list (location, 
                                        packageName, kinds, false);
        List <Class <?>> classes = new ArrayList <> ();
        for (JavaFileObject object : objects) {
            if (!object.getName ().endsWith (".class")) { continue; }
            
            String absoluteClassName = object.toUri ().toString ().replace ('/', '.');
            int relativeIndex = absoluteClassName.indexOf (packageName);
            String className = absoluteClassName.substring (relativeIndex, 
                        absoluteClassName.length () - ".class".length ());
            try {
                classes.add (Class.forName (className, false, cl));
            } catch (ClassNotFoundException cnfe) {}
        }
        
        return classes;
    }
    
    public static final Set <Package> getPackagesInTree (Package root) {
        final String packageName = root.getName ();
        return Arrays.stream  (Package.getPackages ())
             . filter  (p -> p.getName ().startsWith (packageName))
             . collect (Collectors.toSet ());
    }
    
    public static final Map <Class <? extends Annotation>, List <Object>> findAllAnnotations (
            final Collection <Package> packages, 
            final Set <Class <? extends Annotation>> interesting) {
        Map <Class <? extends Annotation>, List <Object>> entries = new HashMap <> ();
        interesting.forEach (i -> entries.put (i, new ArrayList <> ()));
        
        packages.forEach (pkg -> {
            try {
                getClassesFromPackage (pkg).forEach (_class -> {
                    Algorithms.<Class <?>> runBFS (_class, c -> {
                        // Class
                        Arrays.stream (c.getDeclaredAnnotations ()).map (Annotation::annotationType)
                              .filter (interesting::contains).map (entries::get)
                              .forEach (lst -> lst.add (c));
                        // Fields
                        Arrays.stream (c.getDeclaredFields ()).forEach (f -> {
                            Arrays.stream (f.getDeclaredAnnotations ()).map (Annotation::annotationType)
                                  .filter (interesting::contains).map (entries::get)
                                  .forEach (lst -> lst.add (f));
                        });
                        // Methods
                        Arrays.stream (c.getDeclaredMethods ()).forEach (m -> {
                            Arrays.stream (m.getDeclaredAnnotations ()).map (Annotation::annotationType)
                                  .filter (interesting::contains).map (entries::get)
                                  .forEach (lst -> lst.add (m));
                        });
                        
                        return true;
                    }, c -> Arrays.asList (c.getDeclaredClasses ()));
                });
                
            } catch (IOException ioe) {}
        });
        
        return entries;
    }
    
    public static final Map <Class <? extends Annotation>, List <Object>> findAllAnnotations (
            final Package searchPackage, final Set <Class <? extends Annotation>> interesting) {
        return findAllAnnotations (Arrays.asList (searchPackage), interesting);
    }
    
    public static final List <Class <?>> getAllSupers (final Class <?> token) {
        final List <Class <?>> supers = new ArrayList <> ();
        
        Algorithms.<Class <?>> runBFS (token, t -> {
            if (t == null) { return false; }
            
            if (!token.equals (t)) { supers.add (t); }
            return true;
        }, t -> {
            List <Class <?>> children = new ArrayList <> ();
            children.addAll (Arrays.asList (t.getInterfaces ()));
            children.add (t.getSuperclass ());
            return children;
        });
        
        return supers;
    }
    
    public static final List <Class <?>> getAllInterfaces (final Class <?> token) {
        return getAllSupers (token).stream ()
             . filter (t -> t.isInterface ())
             . collect (Collectors.toList ());
    }
    
}
