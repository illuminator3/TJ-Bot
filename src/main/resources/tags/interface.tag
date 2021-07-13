An interface is a way to force a class to implement all of the methods that it declares and it's also a way to manage objects of different types by generalizing them under the same interface {{ user }}. Let's take the interface `Player` for example:
```java
interface Player {
void attack()
}
```

By implementing the `Player` interface to a `Warrior` and `Rogue` class we force those classes to implement the `attack()` method:
```java
class Warrior implements Player {
@Override
void attack() {
System.out.println("Warrior slashes his sword");
}
}

class Rogue implements Player {
@Override
void attack() {
System.out.println("Rogue sneaks behind with a knife");
}
}
```

Now let's say we want to make both our `Warrior` and `Rogue` attack, we would have no way of storing them into a `List` since they are different types, and here is where the interface comes into play. We can simply make a list and add both players to it:
```java
List<Player> players = new ArrayList<>;

players.add(new Warrior());
players.add(new Rogue());

for (Player player: players) {
player.attack();
}
```

By doing this we can iterate through the list of any type of `Player`, as long as they implement the interface we can call their `attack()` methods.