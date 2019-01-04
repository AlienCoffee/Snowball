# Snowball

### Example - how to use

```java
public class Main extends Snowball {             // Main class has to extend `Snowball` class
 
    public static void main (String ... args) { 
        shape (args);                            // Required method to start process of dependency injections
    }
    
    private A instA;                             // Declared local variable that should be injected by Snowball
    
    @Override
    protected void onShaped (String ... args) {  // Method that will be called after end of work of Snowball
        System.out.println (instA);              // Do check that `instA` is assigned
    }
    
    @Snowflake                                   // Annotation on class means that it can be injected in other classes
    public static class A {                      // and all not-initialized fields of it would be assigned with some values
        
        private final C instC;                   // Declared local variable that will be assigned in constructor
        private B instB;                         // Declared (buit not assigned) field that would be initialized with
                                                 // some instance of B class (if there is at least 1 snoflake of such type)
        
        public A (C c) { this.instC = c; }       // For creating instance of A will be used this constructor
        
        @Override
        public String toString () {              // Overrided method to do check of correctness of injections
            return String.format ("Instance of A (@%d, B: %s, C: %s)", hashCode (), 
                                  "" + instB, "" + instC);
        }
        
    }
    
    @Snowflake                                   // Annotation on method means that returned object can be injected
    public static B createB (A a, C c) {         // in other classes, and this method method will be called for instance
        return new B (a, c);
    }
    
    public static class B {                      // This class doesn't have explicit annotation `Snowflake` but
                                                 // actually it has it from it's initialization method
        private final A instA;
        private final C instC;
        
        public B (A a, C c) { 
            this.instA = a; this.instC = c;
        }
        
        @Override
        public String toString () {              // Overrided method to do check of correctness of injections
            return String.format ("Instance of B (@%d, A: %s, C: %s)", hashCode (), 
                                  "" + instA, "" + instC);
        }
        
    }
    
    @Snowflake public static final C             // Annotation on field means that value of it can be used for injections
        C_INSTANCE = new C ();                    
    
    public static class C {
        
        @Override
        public String toString () {              // Overrided method to do check of correctness of injections
            return String.format ("Instance of C (@%d)", hashCode ());
        }
        
    }
    
}
```
