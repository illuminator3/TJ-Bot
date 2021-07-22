An `interface` defines a set of method signatures, as contract. It can greatly increase code modularity. Often, interfaces are property-driven.```java
interface CanWalk {
    void walkLeft();
    void walkRight();
}
```There is no method body. So if a class implements `CanWalk`, he makes the promise to offer those methods:```java
class Player implements CanWalk {
    int x;

    @Override
    void walkLeft() {
        x--;
    }

    @Override
    void walkRight() {
        x++;
    }
}
```Someone could now demand a `CanWalk` instance and use the methods:```java
class Mover {
    static void moveAround(CanWalk canWalk) {
        for (int i = 0; i < 10; i++) {
            canWalk.moveRight();
        }

        canWalk.moveLeft();
        canWalk.moveRight();
    }
}
```Note that the `moveAround` accepts everything that _can walk_.```java
Mover.moveAround(player);
```We could also give it a `Dog`, as long as it implements `CanWalk`.

You have two options to create instances of interfaces:
1. Create a class that `implements` the interface, like `Player`
2. Use an anonymous class:```java
CanWalk canWalk = new CanWalk() {
    @Override
    void walkLeft() {
        System.out.println("Walking left");
    }

    @Override
    void walkRight() {
        System.out.println("Walking right");
    }
};
```An interface that only offers one method is called a **functional interface:**```java
@FunctionalInterface
interface IntOperation {
    int operate(int a, int b);
}
```You have two additional options to create instances of it:
3. Lambda expressions```java
IntOperations operation = (a, b) -> a * b;

System.out.println(operation.operate(5, 3)); // Prints 15
```4. Method references```java
// Method in MathUtil
static int multiply(int a, int b) {
    return a * b;
}

// Use it as
IntOperation operation = MathUtil::multiply;

System.out.println(operation.operate(5, 3)); // Prints 15
```