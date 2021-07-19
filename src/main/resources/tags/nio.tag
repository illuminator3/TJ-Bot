File IO in Java should be done preferably with NIO (Java 7+), revolving around the classes `Files` and `Path`; and not with the old interface `File`, `BufferedReader`, `FileReader` and similar.

NIO is simple to use. The path to a file is represented using the `Path` class:
```java
Path path = Path.of("myFile.txt");
```
All file operations can be found in the `Files` class:
```java
// Reading
List<String> allLines = Files.readAllLines(path);

// or as a single string
String content = Files.readString(path);

// or with a stream
try (Stream<String> stream = Files.lines(path)) {
	stream.forEach(System.out::println);
}

// Writing
Files.write(path, lines);

// or as a single string
Files.writeString(path, "hello world");

// or with extra options
Files.writeString(path, "hello world",
	StandardOpenOption.WRITE,
	StandardOpenOption.CREATE,
	StandardOpenOption.APPEND);
```
If you need more control over the process, you can fallback to the old interface, but prefer using the bridge methods from NIO (`Files.newBufferedReader`, `Files.newInputStream`, `path.toFile()` and similar) to benefit from advantages such as correct encoding and better error detection.

Here is a simple example of how to read a file line-wise with the old interface
```java
try (BufferedReader br = Files.newBufferedReader(path)) {
	String line;
    
	while ((line = br.readLine()) != null) {
		System.out.println(line);
	}
}
```
it is way more verbose than NIO but it gives more control.

You must not forget to close file handles, even in all exceptional cases. Closing a handle manually is very hard, which is why you should always use **try-with-resources** for this to let Java automatically close the handle for you:
```java
// Automatically closed here, even in exceptional cases
try (SomeResource resource = ...) {
	...
}
```
