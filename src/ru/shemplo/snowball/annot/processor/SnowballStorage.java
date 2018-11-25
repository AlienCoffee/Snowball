package ru.shemplo.snowball.annot.processor;

import static java.lang.reflect.Modifier.*;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import ru.shemplo.snowball.annot.Cooler;
import ru.shemplo.snowball.stuctures.Instance;
import ru.shemplo.snowball.stuctures.Pair;
import ru.shemplo.snowball.utils.ClasspathUtils;

public final class SnowballStorage {
    
    SnowballStorage () {}
    
    private Set <Class <?>> 
        VISITED_CLASSES = new HashSet <> ();
    
    public void addVisitedClass (Class <?> visited) {
        VISITED_CLASSES.add (visited);
    }
    
    public boolean isClassVisited (Class <?> token) {
        return VISITED_CLASSES.contains (token);
    }
    
    private final Map <Class <?>, Pair <Integer, Method>> 
        COOLERS = new HashMap<> ();
    
    public final void addCooler (Method method) {
        int mods = method.getModifiers ();
        if (!isStatic (mods) || isPrivate (mods)) {
            System.out.println ("Ignore cooler: " + method.getName ());
            return;
        }
        
        registerCooler (method.getReturnType (), method);
        ClasspathUtils.getAllInterfaces (method.getReturnType ())
                      .forEach (in -> registerCooler (in, method));
    }
    
    private final void registerCooler (Class <?> ret, Method method) {
        int priority = method.getAnnotation (Cooler.class).priority ();
        COOLERS.putIfAbsent (ret, Pair.mp (priority, method));
        COOLERS.compute (ret, (k, v) -> 
            (v.F > priority ? v : Pair.mp (priority, method))
        );
    }
    
    public final List <Pair <Method, List <Class <?>>>> getCoolersWithTypes () {
        Map <Method, List <Class <?>>> coolers = new HashMap <> ();
        COOLERS.entrySet ().stream ().map (Pair::fromMapEntry).forEach (p -> {
                   coolers.putIfAbsent (p.S.S, new ArrayList <> ());
                   coolers.get (p.S.S).add (p.F);
               });
        return coolers.entrySet ().stream ()
             . map (Pair::fromMapEntry)
             . collect (Collectors.toList ());
    }
    
    private final Map <Class <?>, Instance <?>>
        INSTANCES = new HashMap <> ();
    
    public final void addInstance (Class <?> key, Instance <?> instance) {
        if (INSTANCES.put (key, instance) != null) {
            System.out.println ("Replace!");
        }
    }
    
    public final Instance <?> getInstance (Class <?> token) {
        return INSTANCES.get (token);
    }
    
    private final List <Field>
        REQUIRED_FIELDS = new ArrayList <> ();
    
    public final void addField (Field field) {
        Objects.requireNonNull (field);
        REQUIRED_FIELDS.add (field);
    }
    
    public final List <Field> getFields () {
        return REQUIRED_FIELDS;
    }
    
    private final List <Type>
        SNOWFLAKES = new ArrayList <> ();
    
    public final void addSnowflake (Type snowflake) {
        Objects.requireNonNull (snowflake);
        SNOWFLAKES.add (snowflake);
    }
    
    public final List <Type> getSnowflakes () {
        return SNOWFLAKES;
    }
    
    private final List <Runnable> DELAYED_TASKS = new ArrayList <> ();
    
    public void addDelayerTask (Runnable delayedTask) {
        if (delayedTask == null) { return; }
        DELAYED_TASKS.add (delayedTask);
    }
    
    public List <Runnable> getDelayedTasks () {
        return DELAYED_TASKS;
    }
    
}
