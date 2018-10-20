package ru.shemplo.snowball.utils;

import java.util.HashMap;
import java.util.Map;

public class StringManip {

    private static final Map <Character, String> 
        ESCAPE_MAP = new HashMap <> ();
    
    static {
        ESCAPE_MAP.put ('&',  "&amp;");
        ESCAPE_MAP.put ('<',  "&lt;");
        ESCAPE_MAP.put ('>',  "&gt;");
        ESCAPE_MAP.put ('"',  "&quot;");
        ESCAPE_MAP.put ('\'', "&#039;");
    }
    
    public static String escapeChars (String input) {
        if (input == null || input.length () == 0) { return ""; }
        StringBuilder sb = new StringBuilder ();
        for (char c : input.toCharArray ()) {
            if (ESCAPE_MAP.containsKey (c)) {
                sb.append (ESCAPE_MAP.get (c));
            } else { sb.append (c); }
        }
        
        return sb.toString ();
    }
    
}
