# Snowball

### Example - how to use

```java
public class Main extends Snowball { // Main class has to extend `Snowball` class

    public static void main (String ... args) {
        shape (args);                // Required method to start process of dependency injections
    }

    @Snowflake                       // Annotation on class means that it can be injected in other classes
    public static class A {          // and all not-initialized fields of it would be assigned with some values
    
        private B instB;             // Declared (buit not assigned) field that would be initialized with
                                     // some instance of B class (if there is at least 1 snoflake of such type)
    
        @Override
        public String toString () {  // Overrided method to do check of correctness of injections
            return String.format ("Instance of class A (@%d, B: %s)", hashCode (), "" + instB);
        }
    
    }
  
    @Snowflake                        // Annotation of method
    public static B createB () {
        return new B ();
    }
  
    public static class B {
        
    }
  
    public static class C {
     
    }

}
```
