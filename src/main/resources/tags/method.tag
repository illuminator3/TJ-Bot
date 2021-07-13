A method in Java is a block of code that runs every time it is invoked somewhere else in the project {{ user }}. It defines something that can be run, which has a result (or void, for nothing) and can optionally have parameters. All code run in Java is always in a method. An example method looks like
```java
public static void main(String[] args) {
...
}
```
where public is the visibility (see tag on visibility), static defines the method as static (see tag on static) and void means the method returns nothing. main is the name of the method, and `String[] args` is the single parameter of the method. This main method is the most used method in all of Java as it is necessary to have a Java program run.