An abstract class denotes a **yet unfinished class** {{ user }}. For example, it might be only done to 70% but there are still 30% missing. As such, it is used to create a template for other classes that extend it. The extending class can then concentrate on only doing the remaining 30% since 70% are already done. This can greatly reduce code duplication.

In general, a class is `abstract` if it has an `abstract` method. An abstract method is a method that is missing any implementation:
```java
abstract class Animal {
String name;

Animal(String name) {
this.name = name;
}

String getName() { // regular method
return name;
}

abstract void makeNoise(); // No method body
}
```

Since the class is not finished yet and `makeNoise` is lacking any implementation, it is impossible to create instances of it in that state:
```java
Animal animal = new Animal("Buddy"); // Does not compile! Can not instantiate abstract class
```

However, we can use it as template for extending it and get the existing name-functionality for free (`getName()`). But then we are required to implement everything that is still `abstract`, i.e. incomplete like `makeNoise`:
```java
class Dog extends Animal {
Dog() {
super("Buddy"); // all dogs are called Buddy
}

@Override
void makeNoise() {
System.out.println("Wuff Wuff");
}
}
```

And now we can create instances of it and use the methods:
```java
Dog dog = new Dog();
System.out.println(dog.getName()); // Prints Buddy
dog.makeNoise(); // Prints Wuff Wuff
```