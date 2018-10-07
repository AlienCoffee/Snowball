package ru.shemplo.dsau.utils;

public class ByteManip {

    public static byte [] L2B (long value) {
        return convert (value, 8);
    }
    
    public static byte [] I2B (int value) {
        return convert (value, 4);
    }
    
    public static byte [] S2B (short value) {
        return convert (value, 2);
    }
    
    public static byte [] C2B (char value) {
        return convert (value, 2);
    }
    
    private static byte [] convert (long value, int length) {
        byte [] array = new byte [length];
        for (int i = length - 1; i >= 0; i--) {
            array [i] = (byte) (value & 0xff);
            value >>>= 8;
        }
        
        return array;
    }
    
    public static long B2L (byte [] bytes) {
        return backvert (bytes, 8);
    }
    
    public static int B2I (byte [] bytes) {
        return (int) backvert (bytes, 4);
    }
    
    private static long backvert (byte [] bytes, int length) {
        int limit = Math.min (length, bytes.length);
        long result = 0;
        
        for (int i = 0; i < limit; i++) {
            result = (result << 8) | (bytes [i] & 0xffL);
        }
        
        return result;
    }
    
}