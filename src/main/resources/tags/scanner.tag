Mixing any `nextXXX` method with `nextLine` from the `Scanner` class for user input, will not ask you for input again but instead result in an empty line read by `nextLine`.

To prevent this, when reading user input, always only use `nextLine`. If you need an `int`, do
```java
int value = Integer.parseInt(scanner.nextLine());
```
instead of using `nextInt`.

Assume the following:
```java
Scanner scanner = new Scanner(System.in);

System.out.println("Enter your age:");
int age = scanner.nextInt();
System.out.println("Enter your name:");
String name = scanner.nextLine();

System.out.println("Hello " + name + ", you are " + age + " years old");
```
When executing this code, you will be asked to enter an age, suppose you enter `20`.
However, the code will not ask you to actually input a name and the output will be:
```
Hello , you are 20 years old.
```
The reason why is that when you hit the enter button, your actual input is
```
20\n
```
and not just `20`. A call to `nextInt` will now consume the `20` and leave the newline symbol `\n` in the internal input buffer of `System.in`. The call to `nextLine` will now not lead to a new input, since there is still unread input left in `System.in`. So it will read the `\n`, leading to an empty input.

So every user input is not only a number, but a **full line**. As such, it makes much more sense to also use `nextLine()`, even if reading just an age. The corrected code which works as intended is:
```java
Scanner scanner = new Scanner(System.in);

System.out.println("Enter your age:");
// Now nextLine, not nextInt anymore
int age = Integer.parseInt(scanner.nextLine());
System.out.println("Enter your name:");
String name = scanner.nextLine();

System.out.println("Hello " + name + ", you are " + age + " years old");
```
The `nextXXX` methods, such as `nextInt` can be useful when reading multi-input from a single line. For example when you enter `20 John` in a single line.
