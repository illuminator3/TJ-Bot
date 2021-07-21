TL;DR: Comparing some types with `==`, (especially `String` references) can have confusing results. Always use `a.equals(b)` for Strings.

A variable's type is either a primitive or a reference:
- Primitive values are, for example, `0` or `1` for `int`, `true` or `false` for `boolean`, `'a'` or `'*'` for `char`...
- Reference values are either `null` or a _reference_ to an object. e.g. The value of `s` in `String s = "Foo";` is reference to a String object, not the object itself.

Using `==` compares the _values_ of two variables. If two reference values are the same, they refer to the same object or are both null.

Using `a.equals(b)` calls a method on `a` that compares its contents to the object referenced by `b`. You are asking if the two objects are alike.

Imagine you know the following three people:
- `jane`
- `bob`
- Bob's identical twin brother `michael`, also known as `mike`.

The following are examples of using `==` and `.equals`:
- `jane == bob` - `false`. They are not the same person
- `jane.equals(bob)` - `false`. They are not alike
- `bob == michael` - `false`. They are not the same person
- `bob.equals(michael)` - `true`. They are alike
- `michael == mike` - `true`. They are the same person

Strings are special in Java and, because of this, comparing references to Strings can be surprising. Though there are rare cases where it is useful and necessary to use `==` with String references, you should almost always be using `.equals(Object)` to compare the Strings of two references. For two strings `a` and `b` use `a.eqauls(b)` rather than `a == b`.

Note: `a.equals(b)` is intended to see if the two objects referenced by `a` and `b` are alike. How alike they must be depends on the implementation of `equals` for the class of `a`. The default implementation of `equals`, has the same behavior as using `==`.