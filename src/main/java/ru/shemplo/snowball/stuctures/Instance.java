package ru.shemplo.snowball.stuctures;


public class Instance <T> {
    
    public static <T> Instance <T> valueOf (T object) {
        if (object == null) {
            String message = "Unable to create Instance of NULL";
            throw new IllegalArgumentException (message);
        }
        
        return new Instance <T> (object);
    }
    
    private final T OBJECT;
    
    private Instance (T object) {
        this.OBJECT = object;
    }
    
    @Override
    public boolean equals (Object obj) {
        return OBJECT.equals (obj);
    }
    
    @Override
    public int hashCode () {
        return OBJECT.hashCode ();
    }
    
    public T get () {
        return OBJECT;
    }
    
    @SuppressWarnings ("unchecked")
    public final Class <? extends T> getClassToken () {
        return (Class <? extends T>) OBJECT.getClass ();
    }
    
}
