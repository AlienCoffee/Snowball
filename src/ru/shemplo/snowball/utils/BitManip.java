package ru.shemplo.snowball.utils;

import java.util.concurrent.atomic.AtomicInteger;

public class BitManip {
    
    private static AtomicInteger counter = new AtomicInteger (0);
    
    public static int nextID () {
        while (true) {
            int current = counter.intValue ();
            if (counter.compareAndSet (current, current + 1)) {
                return current;
            }
        }
    }
    
    public static int getBit (int number, int index) {
        return 0b1 & (number >>> index);
    }
    
    public static int getBits (int number, int index, int length) {
        int offset = 32 - length - index;
        return (number << offset) >>> (index + offset);
    }
    
    public static int fillLast (int number, int value) {
        return ((value & 0b1) << number) - 1;
    }
    
}
