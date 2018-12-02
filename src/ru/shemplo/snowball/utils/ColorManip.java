package ru.shemplo.snowball.utils;

import static java.lang.Math.*;
import java.awt.Color;
import java.util.Random;

public class ColorManip {

    private static final double POLAR_DELTA    = 2 * PI / 3,
                                SQ_OF_DIAMETER = 2 * 2;
    private static final double [][] STOP_POLARS = {
        {cos (0),            sin (0)},           // r
        {cos (POLAR_DELTA),  sin (POLAR_DELTA)}, // g
        {cos (-POLAR_DELTA), sin (-POLAR_DELTA)} // b
    };
    
    public static Color getSpectrumColor (double radius, double radians) {
        if (radius > 1) { return Color.BLACK; }
        
        int [] stops = new int [STOP_POLARS.length];
        for (int i = 0; i < STOP_POLARS.length; i++) {
            double dX = cos (radians) - STOP_POLARS [i][0],
                   dY = sin (radians) - STOP_POLARS [i][1];
            double polarDistance = dX * dX + dY * dY;
            stops [i] = (int) (255 - (polarDistance / SQ_OF_DIAMETER * 255));
        }
        
        return new Color (stops [0], stops [1], stops [2]);
    }
    
    private static final Random RANDOM = new Random ();
    
    public static final javafx.scene.paint.Color getRandomColor (int from, int to) {
        return javafx.scene.paint.Color.rgb (
                from + RANDOM.nextInt (to - from), 
                from + RANDOM.nextInt (to - from), 
                from + RANDOM.nextInt (to - from)
             );
    }
    
    public static final javafx.scene.paint.Color getRandomColor () {
        return getRandomColor (0, 256);
    }
    
}
