`static` is a keyword in java used to denote that a method or class field is not instance-specific and instead accessed through the class itself {{ user }}.

A static field is the same throughout all instances of a class. Any change to a static field is applied to all instances of the class. An example of this can be seen by the code below.
```java
public class Example {
public static int i = 7;

public static void main(String[] args) {
Example ex1 = new Example();
Example ex2 = new Example();
//both of these will print 7
System.out.println(ex1.i);
System.out.println(ex2.i);
ex1.i++; //increasing i by 1
System.out.println(ex2.i); //this will print out 8 as i is a static field and shared throughout all instances of Example
}
}
```

A static field or method can also be accessed through the class's name. It is recommended to access static methods and fields through the class rather than an instance to signify in your code that the said thing is indeed static and to improve readability. The same `i` from the previous example can (and should) be accessed with `Example.i`. This uses the class name and shows that the `i` variable is static.

A static method is a method that does not access anything from the class that requires an instance. It is called from the class itself and is not bound to any specific instance.
```java
public class OtherExample {
public String data;

public static int getDataLength() {
//this is invalid because data is tied to an instance of OtherExample and cannot be accessed statically.
return data.length();
}
}
```

There are many reasons for using the `static` keyword in your code. If you want an object that is shared between all instances of a class, `static` is right for you. If you want to use a helper method without needing an instance of the class (like a Util class), `static` is right for you.