The Liskov Substitution Principle (LSP):

`If Type B extends Type A, then an object of Type B may be used wherever an object of Type A is expected.`

Robert C. Martin's inclusion of the principle in his idea of S.O.L.I.D. is summarized as:

`Subtypes must be substitutable for their base types.`

This type-subtype relationship may be observed through the one between an interface and another one extending it, as a rule of thumb. The _"IS-A"_ relationship between the two guarantees substitutability, an important principle in object-oriented programming. We understand this when looking at examples in Java code, such as `ArrayList`'s _IS-A_ relationship with `List`, or when noting how the `List` interface _IS-A_ `Collection`.
ArrayList is a List implementation so List should be used as a type, as a rule.

Reading:
(2020) Baeldung CS (summary): <https://www.baeldung.com/cs/liskov-substitution-principle>
(2020) Baeldung Java: <https://www.baeldung.com/java-liskov-substitution-principle>
(2020) Example code: <https://reflectoring.io/lsp-explained/>
(2018) Stackify SOLID article: <https://stackify.com/solid-design-liskov-substitution-principle/>
(2018) Spigot forum post: <https://www.spigotmc.org/threads/the-liskov-substitution-principle-and-why-it-is-useful.332119/>
(2000) Robert C. Martin's paper: <https://fi.ort.edu.uy/innovaportal/file/2032/1/design_principles.pdf>
