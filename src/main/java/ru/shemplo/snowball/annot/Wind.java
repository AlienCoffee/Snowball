package ru.shemplo.snowball.annot;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;


@Target (TYPE)
@Retention (RUNTIME)
public @interface Wind {
    
    Class <?> [] blow ();
    
}
