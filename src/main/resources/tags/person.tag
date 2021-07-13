A classic example of a simple Java class `Person` that has two fields `String name` and `int age` {{ user }}, with corresponding getter and setter methods:
```java
public class Person {
private String name;
private int age;

public Person(String name, int age) {
this.name = name;
this.age = age;
}

public String getName() {
return name;
}

public int getAge() {
return age;
}

public void setName(String name) {
this.name = name;
}

public void setAge(int age) {
this.age = age;
}
}
```

Simple usage example:
```java
Person person = new Person("John", 20);
System.out.println(person.getName()); // John
System.out.println(person.getAge()); // 20
person.setAge(21);
System.out.println(person.getAge()); // 21
```