package ru.shemplo.snowball.utils;

import static ru.shemplo.snowball.utils.fun.StreamUtils.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

import java.io.BufferedReader;
import java.io.IOException;

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
    
    public static List <String> splitOnTokens (String input) {
        StringTokenizer st = new StringTokenizer (input);
        List <String> tokens = whilst (StringTokenizer::hasMoreTokens, 
                                       StringTokenizer::nextToken, st)
                             . collect (Collectors.toList ());
        return tokens;
    }
    
    public static String fetchNonEmptyLine (BufferedReader br) throws IOException {
        String line = null;
        while ((line = br.readLine ()) != null) {
            if (line.length () == 0) { continue; }
            return line;
        }
        
        return null;
    }
    
    public static int compare (String a, String b) {
        throw new UnsupportedOperationException ();
    }
    
}
