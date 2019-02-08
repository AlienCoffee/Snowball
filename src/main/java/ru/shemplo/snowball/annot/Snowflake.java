package ru.shemplo.snowball.annot;

import static java.lang.annotation.ElementType.*;
import static java.lang.annotation.RetentionPolicy.*;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/**
 * For TYPE:
 * Object of such type will be initialized with 
 * constructor and all fields will be assigned 
 * with objects of required types.
 * 
 * Instance of this type would be used in
 * assignments of fields in other objects.
 * 
 * For METHOD:
 * Will use object from return type in
 * assignments of fields in objects.
 * 
 * For FIELD:
 * Will use object of type of field in
 * assignments of other fields in objects.
 * 
 * @author Shemplo
 *
 */
@Retention (RUNTIME)
@Target ({TYPE, METHOD, FIELD})
public @interface Snowflake {
    
    /**
     * In case of conflict of types for
     * assignment is some field will be used
     * snowflake with bigger priority. This
     * feature can be used for testing, when
     * default implementation should be replaced.
     * 
     * @return priority of this snowflake
     * 
     */
    int priority () default 0;
    
    /**
     * In case of positive value each time 
     * when this snowflake will be asked 
     * it will be initialized again.
     * 
     * @return whether snowflake should be initialized each time
     * 
     */
    boolean refresh () default false;
    
    /**
     * Assigns that marked field will be initialized manually.
     * It can be used when you need to declare field that doesn't
     * require dependency injection.
     * 
     * @return ignore this field or not
     * 
     */
    boolean manual () default false;
    
}
