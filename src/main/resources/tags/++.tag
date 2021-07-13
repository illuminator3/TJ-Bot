**How to use** `var++` **and** `++var` {{ user }}:

`++` increments a value by one.
`--` decrements a value by one.

The difference between `var++` and `++var` is when the increment happens
`var++` is a post-increment (increment happens after value is used)
`++var` is a pre-increment (increment happens before value is used)

if `var++`is used as an expression, the increment happens, after it's value is being read:
```java
int a = 3;
int b = a++; // b is now 3, and a is 4

// The above is the same as this following snippet

int a = 3;
int b = a;
a = a + 1; //increment happens after the value of a is used
```

meanwhile, if you use `++var`, the increment happens first, and then the variable's value is used:
```java
int a = 3;
int b = ++a; // b is now 4 and a is 4
```

means the same as:
```java
int a = 3;
a = a + 1; //increment happens before value of a is used
int b = a;
```

It is generally advised, to only use `var++` for very simple statements, as it's an easy way for bugs to sneak into your program.
Don't try write smart code, write simple code, it'll make your life easier.