package ru.shemplo.snowball.annot;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;


@Retention (RUNTIME)
@Target (TYPE)
public @interface Snowflake {
    
    boolean refresh () default false;
    
}
