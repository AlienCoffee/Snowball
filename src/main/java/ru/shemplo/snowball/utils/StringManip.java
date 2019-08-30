package ru.shemplo.snowball.utils;

import static ru.shemplo.snowball.utils.fp.StreamUtils.*;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

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
    
    public static String readAsString (InputStream is, Charset charset) {
        Objects.requireNonNull (is);
        
        StringBuilder sb = new StringBuilder ();
        try (
            Reader r = new InputStreamReader (is, charset);
            BufferedReader br = new BufferedReader (r);
        ) {
            String line = null;
            while ((line = br.readLine ()) != null) {
                sb.append (line).append ("\n");
            }
        } catch (IOException ioe) {
            String message = "Failed to read input stream";
            new IllegalArgumentException (message);
        }
        
        return sb.toString ();
    }
    
    public static String readAsString (InputStream is) {
        return readAsString (is, StandardCharsets.UTF_8);
    }
    
    public static String substringBeforeFirst (String string, String delimiter) {
        int index = string.indexOf (delimiter);
        return index != -1 ? string.substring (0, index) : string;
    }
    
    public static String substringBeforeLast (String string, String delimiter) {
        int index = string.lastIndexOf (delimiter);
        return index != -1 ? string.substring (0, index) : string;
    }
    
    public static String substringAfterFirst (String string, String delimiter) {
        int index = string.lastIndexOf (delimiter);
        return index != -1 ? string.substring (index + delimiter.length ()) : string;
    }
    
    public static String substringAfterLast (String string, String delimiter) {
        int index = string.lastIndexOf (delimiter);
        return index != -1 ? string.substring (index + delimiter.length ()) : string;
    }
    
}
