{{ user }}
There are several conventions for type parameter naming. There are some names that are fairly standard across conventions.

The most commonly used type parameter names are:
**E** - Element (used extensively by the Java Collections Framework)
**K** - Key
**N** - Number
**T** - Type
**V** - Value
**R** - Result (used extensively by the Streams api)
**A** - Accumulator (used extensively by the Streams api)
**S**, **U**, **V** etc. - 2nd, 3rd, 4th types

The reason for single letter usage is given by Oracle as By convention, type parameter names are single, uppercase letters. This stands in sharp contrast to the variable naming conventions that you already know about, and with good reason: Without this convention, it would be difficult to tell the difference between a type variable and an ordinary class or interface name. <https://docs.oracle.com/javase/tutorial/java/generics/types.html>

However, the google style guide allows for an extension to that:
• A single capital letter, optionally followed by a single numeral (such as `E`, `T`, `X`, `T2`)
• A name in the form used for classes, followed by the capital letter `T` (examples: `RequestT`, `FooBarT`)
<https://google.github.io/styleguide/javaguide.html#s5.2.8-type-variable-names>