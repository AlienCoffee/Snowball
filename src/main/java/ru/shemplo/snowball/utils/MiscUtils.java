package ru.shemplo.snowball.utils;

import java.lang.reflect.Method;

public class MiscUtils {
    
    @SuppressWarnings ("unchecked")
    public static <I, R> R cast (I object) {
        return (R) object;
    }
    
    public static Method getMethod () {
        StackTraceElement need = Thread.currentThread ()
                               . getStackTrace () [2];
        try {            
            final Class <?> type = Class.forName (need.getClassName ());
            for (Method method : type.getDeclaredMethods ()) {
                if (method.getName ().equals (need.getMethodName ())) {
                    return method;
                }
            }
        } catch (ClassNotFoundException cnfe) {
            return null;
        }
        
        return null;
    }
    
}
