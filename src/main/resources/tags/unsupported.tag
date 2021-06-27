The java.lang.UnsupportedClassVersionError occurs when the class compiled using a higher version then the currently installed java executable on the system. Meaning, Java is backwards compatible, but not forward. It could happen if you've got two versions of Java installed on your system as well, f.e. javac and java versions don't match. It is often caused by the two versions both set on the PATH environment variable, the first place to look to try and solve the issue.

Class executables are marked with "major.minor" version at bytes 6 and 7, respectfully. This makes Java 8 exactly version 52.0.
{{ user }}
```java
╔═══════════════╤═══╤═══╤═══╤═══╤══╤══╤══╤══╤══╤══╤══╤══╤══╤══╗
║ Major Version │45 │46 │47 │48 │49│50│51│52│53│54│55│56│57│58║
╟───────────────┼───┼───┼───┼───┼──┼──┼──┼──┼──┼──┼──┼──┼──┼──║
║ Java Version  │1.1│1.2│1.3│1.4│5 │6 │7 │8 │9 │10│11│12│13│14║
╚═══════════════╧═══╧═══╧═══╧═══╧══╧══╧══╧══╧══╧══╧══╧══╧══╧══╝
```