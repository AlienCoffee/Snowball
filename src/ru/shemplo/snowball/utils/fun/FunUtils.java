package ru.shemplo.snowball.utils.fun;

public class FunUtils {
    
    public static boolean just (boolean state, Runnable onTrue) {
        if (state) { onTrue.run (); }
        return state;
    }
    
    public static <I1, I2> boolean either (boolean state, 
            Runnable onTrue, Runnable onFalse) {
        if (state) { onTrue.run (); }
        else { onFalse.run (); }
        return state;
    }
    
}
