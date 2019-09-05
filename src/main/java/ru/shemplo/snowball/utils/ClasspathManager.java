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
import ru.shemplo.snowball.utils.fp.StreamUtils;

public class ClasspathManager {
    
    private boolean destroyed = false;
    
    public boolean isDestroyed () {
        return destroyed;
    }
    
    private final List <Class <?>> classes = new ArrayList <> ();
    
    public ClasspathManager () { this (Collections.emptyList ()); }
    
    public ClasspathManager (List <String> includePrefixes) {
        try   { loadClasses (includePrefixes); } 
        catch (IOException ioe) {
            throw new IllegalStateException (ioe);
        }
    }
    
    private void loadClasses (List <String> includePrefixes) throws IOException {
        final ClassLoader    classLoader    = Thread.currentThread  ().getContextClassLoader ();
        final URLClassLoader urlClassLoader = new URLClassLoader (getJarURLs (), classLoader);
        Arrays.asList  (loadFromProject (classLoader, includePrefixes), 
                        loadFromJARs (urlClassLoader, includePrefixes)).stream ()
              .flatMap (List::stream).filter (Objects::nonNull).distinct ()
              .forEach (classes::add);
        classes.sort (Comparator.comparing (Class::getName));
        //System.out.println (String.format ("%d classes loaded", classes.size ()));
    }
    
    private List <Class <?>> loadFromProject (ClassLoader cl, List <String> includePrefixes) throws IOException {
        List <File> rootFiles = StreamUtils.whilst (Enumeration::hasMoreElements, Enumeration::nextElement, 
                                                    cl.getResources (""))
           . map (URL::getFile).map (this::decodeUnchecked).map (File::new)
           . filter (Objects::nonNull).filter (File::exists)
           . collect (Collectors.toList ());
        
        List <Class <?>> classes = new ArrayList <> ();
        rootFiles.forEach (file -> {
            Algorithms.runBFS (file, f -> {
                String packageName = f.getAbsolutePath ().replace (file.getAbsolutePath (), "");
                packageName = StringManip.substringBeforeLast (packageName, File.separator);
                if (packageName.length () > 1) {
                    packageName = packageName.substring (1).replace (File.separator, ".");
                }
                
                if (!f.isDirectory () && f.isFile ()) {
                    final String fileName = f.getName ();
                    if (!fileName.endsWith (".class")) {
                        return false; // non-java file
                    }
                    
                    
                    String className = fileName.substring (
                        0, fileName.length () - ".class".length ()
                    );
                    
                    className = new StringBuilder (packageName)
                              . append (".").append (className)
                              . toString ();

                    if (className.endsWith ("package-info") 
                        || className.endsWith ("module-info")) {
                        return false; // java but non-class file
                    }
                    
                    try {
                        classes.add (Class.forName (className, false, cl));
                    } catch (ClassNotFoundException | NoClassDefFoundError cnfe) {
                        return false;
                    }
                } else if (f.isDirectory () && includePrefixes.size () > 0) {
                    for (String prefix : includePrefixes) {
                        if (packageName.startsWith (prefix) || prefix.startsWith (packageName)) {
                            return true;
                        }
                    }
                }
                
                return false;
            }, f -> Arrays.asList (f.listFiles ()));
        });
        
        return classes;
    }
    
    private List <Class <?>> loadFromJARs (URLClassLoader cl, List <String> includePrefixes) {
        return Arrays.stream (cl.getURLs ())
             . map     (URL::getPath)
             . map     (this::decodeUnchecked)
             . map     (this::makeJarFileUnchecked)
             . filter  (Objects::nonNull)
             . map     (this::getEntriesFromJAR)
             . flatMap (List::stream)
             . map     (JarEntry::getName)
             . filter  (n -> n.endsWith (".class"))
             . map     (n -> n.substring (0, n.length () - ".class".length ()))
             . filter  (n -> !n.endsWith ("package-info") && !n.endsWith ("module-info"))
             . map     (n -> n.replace ("/", "."))
             . map     (n -> Pair.mp (n.lastIndexOf ("."), n))
             . filter  (p -> p.F > -1)
             . map     (p -> Pair.mp (p.S.substring (0, p.F), p.S.substring (p.F + 1)))
             . map     (p -> String.format ("%s.%s", p.F, p.S))
             . filter  (n -> {
                 for (String prefix : includePrefixes) {
                     if (n.startsWith (prefix)) {
                         return true;
                     }
                 }
                 
                 return false;
             })
             . map     (n -> getClassForNameUnchecked (n, cl))
             . collect (Collectors.toList ());
    }
    
    private final String decodeUnchecked (String url) {
        try   { return URLDecoder.decode (url, "UTF-8"); } 
        catch (UnsupportedEncodingException uee) {/*impossible*/}
        
        return null;
    }
    
    private final Class <?> getClassForNameUnchecked (
            String name, ClassLoader classLoader) {
        try   { return Class.forName (name, false, classLoader); } 
        catch (ClassNotFoundException | NoClassDefFoundError cnfe) {}
        catch (UnsupportedClassVersionError ucve) {}
        
        return null;
    }
    
    private final URL [] getJarURLs () {
        final String classpath = System.getProperty ("java.class.path");
        return Stream.of (classpath.split (File.pathSeparator))
             . filter   (path -> path.endsWith (".jar"))
             . map      (File::new)
             . map      (File::toURI)
             . map      (this::getURLUnhecked)
             . filter   (Objects::nonNull)
             . distinct ()
             . collect  (Collectors.toList ())
             . toArray  (new URL [0]);
    }
    
    private final URL getURLUnhecked (URI uri) {
        try   { return uri.toURL (); } 
        catch (MalformedURLException murle) {}
        
        return null;
    }
    
    private final JarFile makeJarFileUnchecked (String file) {
        try   { return new JarFile (file); } 
        catch (IOException io) {}
        
        return null;
    }
    
    public final List <JarEntry> getEntriesFromJAR (JarFile jarFile) {
        return Stream.of (jarFile).map (JarFile::entries)
             . flatMap (en -> StreamUtils.whilst (Enumeration::hasMoreElements, 
                                                  Enumeration::nextElement, en))
             . collect (Collectors.toList ());
    }
    
    public void destroy () {
        if (!isDestroyed ()) {
            classes.clear (); // to free space
            destroyed = true;
        }
    }
    
    public Map <Class <? extends Annotation>, List <Object>> findObjectsWithAnnotation (
        Set <Class <? extends Annotation>> interesting
    ) {
        Map <Class <? extends Annotation>, List <Object>> entries = new HashMap <> ();
        interesting.forEach (i -> entries.put (i, new ArrayList <> ()));
        
        classes.forEach (_class -> {
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
        
        return entries;
    }
    
}
