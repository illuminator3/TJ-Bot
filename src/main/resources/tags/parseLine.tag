Reading files that contain data is easy to accomplish with proper OOP.
Let us suppose we have a file containing:
```
John Doe 20
Jane Doe 17
Bob Doe 30
```
In order to read and work with that data, we create a simple `Person` class:
```java
public class Person {
	private final String firstName;
	private final String lastName;
	private final int age;

	// Constructor, getter omitted 
}
```
We want to read the file line by line, so we add a method `parseLine` to the `Person` class that, given a line, creates the corresponding `Person`:
```java
public static Person parseLine(String line) {
	String[] data = line.split(" ");
	String firstName = data[0];
	String lastName = data[1];
	int age = Integer.parseLine(data[2]);

	return new Person(firstName, secondName, age);
}
```
We read the file line by line, call `parseLine` and collect the resulting people into a `List<Person>`:
```java
// Stream solution
List<Person> persons = Files.lines(Paths.get("myFile.txt"))
	.map(Person::parseLine)
	.collect(Collectors.toList());

// Classic solution
List<String> lines = Files.readAllLines(Paths.get("myFile.txt"));
List<Person> persons = new ArrayList<>();

for (String line : lines) {
	persons.add(Person.parseLine(line));
}
```
We could now easily work with that data, for example increasing the age of John:
```java
persons.get(0).setAge(25);
```
For updating the file, we add a `toLine()` method to Person:
```java
public String toLine() {
    return String.join(" ", firstName, lastName, String.valueOf(age));
}
```
Lastly, we create a list of the lines we want to write:
```java
List<String> lines = persons.stream()
	.map(Person::toLine)
	.collect(Collectors.toList());
```
and write them back to the file:
```java
Files.write(Paths.get("myFile.txt"), lines);
```
