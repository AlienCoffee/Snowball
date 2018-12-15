package ru.shemplo.snowball.utils;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.annotation.Annotation;
import java.net.*;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import ru.shemplo.snowball.stuctures.Pair;
import ru.shemplo.snowball.utils.fun.StreamUtils;

public class ClasspathUtils {
    
    /**
     * ...
     * 
     * @param pkg
     * 
     * @return
     * 
     */
    public static final List <Class <?>> getClassesFromPackage (Package pkg) throws IOException {
        final ClassLoader classLoader = Thread.currentThread ().getContextClassLoader ();
        final String path = pkg.getName ().replace (".", "/");
        
        return Stream.concat (
                StreamUtils.whilst (Enumeration::hasMoreElements, Enumeration::nextElement, 
                                       classLoader.getResources (path))
                . map     (URL::getFile)
                . map     (ClasspathUtils::decodeUnchecked)
                . map     (File::new)
                . filter  (Objects::nonNull)
                . filter  (File::exists)
                . map     (File::listFiles)
                . flatMap (Stream::of)
                . filter  (File::isFile)
                . filter  (f -> f.getName ().endsWith (".class"))
                . map     (File::getName)
                . map     (n -> n.substring (0, n.length () - ".class".length ()))
                . filter  (n -> !"package-info".equals (n))
                . map     (n -> pkg.getName ().concat (".").concat (n))
                . map     (n -> getClassForNameUnchecked (n, classLoader))
                . filter  (Objects::nonNull)
                , 
                getClassesFromJAR (pkg)
                . filter (Objects::nonNull)
             )
             . distinct ()
             . collect (Collectors.toList ());
    }
    
    private static final String decodeUnchecked (String url) {
        try   { return URLDecoder.decode (url, "UTF-8"); } 
        catch (UnsupportedEncodingException uee) {/*impossible*/}
        
        return null;
    }
    
    private static final Class <?> getClassForNameUnchecked (
            String name, ClassLoader classLoader) {
        try   { return Class.forName (name, false, classLoader); } 
        catch (ClassNotFoundException cnfe) {}
        
        return null;
    }
    
    private static final Stream <Class <?>> getClassesFromJAR (Package pkg) {
        final ClassLoader classLoader = Thread.currentThread  ()
                                      . getContextClassLoader ();
        final URLClassLoader urlClassLoader 
            = new URLClassLoader (getJarURLs (), classLoader);
        final String packageName = pkg.getName ();
        
        return Arrays.stream (urlClassLoader.getURLs ())
             . map     (URL::getPath)
             . map     (ClasspathUtils::decodeUnchecked)
             . map     (ClasspathUtils::makeJarFileUnchecked)
             . filter  (Objects::nonNull)
             . map     (JarFile::entries)
             . flatMap (en -> StreamUtils.whilst (Enumeration::hasMoreElements, 
                                                  Enumeration::nextElement, en))
             . map     (JarEntry::getName)
             . filter  (n -> n.endsWith (".class"))
             . map     (n -> n.substring (0, n.length () - ".class".length ()))
             . map     (n -> n.replace ("/", "."))
             . map     (n -> Pair.mp (n.lastIndexOf ("."), n))
             . filter  (p -> p.F > -1)
             . map     (p -> Pair.mp (p.S.substring (0, p.F), p.S.substring (p.F + 1)))
             . filter  (p -> packageName.equals (p.F))
             . map     (p -> String.format ("%s.%s", p.F, p.S))
             . map     (n -> getClassForNameUnchecked (n, urlClassLoader));
    }
    
    private static final URL [] getJarURLs () {
        final String classpath = System.getProperty ("java.class.path");
        return Stream.of (classpath.split (File.pathSeparator))
             . filter   (path -> path.endsWith (".jar"))
             . map      (File::new)
             . map      (File::toURI)
             . map      (ClasspathUtils::getURLUnhecked)
             . filter   (Objects::nonNull)
             . distinct ()
             . collect  (Collectors.toList ())
             . toArray  (new URL [0]);
    }
    
    private static final JarFile makeJarFileUnchecked (String file) {
        try   { return new JarFile (file); } 
        catch (IOException io) {}
        
        return null;
    }
    
    private static final URL getURLUnhecked (URI uri) {
        try   { return uri.toURL (); } 
        catch (MalformedURLException murle) {}
        
        return null;
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
