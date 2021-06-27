{{ user }}
`this` is a keyword in Java that references the current instance of the class.

`this` can be passed to other methods to reference itself. An example of this is show here:
```java
public class Foo {
public Bar getBar() {
return new Bar(this); //references this instance
}
}

public class Bar {
public Foo foo;

public Bar(Foo object) {
foo = object;
}

public static void main(String[] args) {
Foo foo = new Foo();
Bar bar = foo.getBar();
boolean same = (foo == bar.foo); //true because both reference the same Foo object
}
}```


`this` can also be used with **variable scope**. Using the variables from the previous example, there is the `foo` *field* in `Bar`, and the `object` *local variable* in the `Bar` constructor. A **field** is a variable that resides in the class itself. A **local variable** is a variable that is created inside of a method. Using `this`, you can now change the name of the Foo `object` in the `Bar` constructor to also be `foo`. When you have a local variable and field with the same name, Java differentiates these using `this`. You can reference the `foo` field with `this.foo` and the local variable with regular `foo`. The updated constructor would then look like
```java
public Bar(Foo foo) {
this.foo = foo; //this sets the field foo to the value of the local variable foo in the constructor
}
```