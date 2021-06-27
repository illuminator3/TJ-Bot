package gg.discord.tj.bot.core;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.InteractionCreateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import discord4j.rest.service.ApplicationService;
import discord4j.rest.util.ApplicationCommandOptionType;
import gg.discord.tj.bot.db.Database;
import gg.discord.tj.bot.util.CountableMap;
import gg.discord.tj.bot.util.CountableTreeMap;
import gg.discord.tj.bot.util.Tuple;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@SuppressWarnings("ConstantConditions")
@RequiredArgsConstructor
@Getter
public class TJBot
    implements Bot
{
    private static final Pattern HELP_CHANNEL_NAME_PATTERN = Pattern.compile("help|review");
    private static final Comparator<Map.Entry<?, Long>> ENTRY_LONG_VALUE_COMPARATOR = (o1, o2) -> o2.getValue().compareTo(o1.getValue());
    private static final StringBuilder NO_ENTRIES_STRINGBUILDER = new StringBuilder("No entries");
    private static final Duration DURATION_30D = Duration.ofDays(30);
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final ApplicationCommandRequest TOP_HELPERS_COMMAND = ApplicationCommandRequest.builder()
            .name("tophelpers")
            .description("View the top helpers of last month")
            .addOption(ApplicationCommandOptionData.builder()
                    .name("limit")
                    .description("The amount of top helpers")
                    .required(true)
                    .type(ApplicationCommandOptionType.INTEGER.getValue())
                    .build())
            .build();
    private static final ApplicationCommandInteractionOptionValue APPLICATION_COMMAND_INTERACTION_OPTION_VALUE_LONG_10 =
            new ApplicationCommandInteractionOptionValue(null, null, ApplicationCommandOptionType.INTEGER.getValue(), "10");

    private final String token;

    private GatewayDiscordClient client;
    private ExecutorService executorService;
    private RestClient restClient;
    private long applicationId;

    @SneakyThrows
    @Override
    public void start()
    {
        Database.DATABASE.establishConnection(Path.of("tjdatabase.db").toFile().getCanonicalPath());

        Database.DATABASE.update("""
                CREATE TABLE IF NOT EXISTS messages (
                    user long,
                    timestamp long
                );
                """);

        executorService = Executors.newCachedThreadPool();
        (client = DiscordClient.create(token)
                .login()
                .block()).on(MessageCreateEvent.class).subscribe(e -> {
                    Optional<Snowflake> guildId = e.getGuildId();

                    if (e.getMember().isPresent() && guildId.isPresent() && guildId.get().asLong() == 272761734820003841L && HELP_CHANNEL_NAME_PATTERN.matcher(((TextChannel) e.getMessage().getChannel().block()).getName()).find())
                        Database.DATABASE.safeUpdate("INSERT INTO messages (user, timestamp) VALUES (%d, %d)", e.getMember().get().getId().asLong(), System.currentTimeMillis());
                });

        Map<String, String> tags = new HashMap<>();

        tags.put("ask",
"Please don't ask to ask{{ user }}, nor only say hello, just ask your actual question right away. <https://www.dontasktoask.com/>"
);


        tags.put("w3",
"Please check out <https://www.w3schools.com/java/default.asp>{{ user }}"
);

        tags.put("visibility-chart",
"Visibility Chart:{{ user }}\n```\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2564\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2564\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2564\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2564\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2564\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2557\n\u2551           \u2502 Class \u2502 Package \u2502 Subclass \u2502 Subclass \u2502 World  \u2551\n\u2551           \u2502       \u2502         \u2502(same pkg)\u2502(diff pkg)\u2502        \u2551\n\u255F\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u253C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u253C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u253C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u253C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u253C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500╢\n\u2551public     \u2502   +   \u2502    +    \u2502    +     \u2502    +     \u2502   +    \u2551\n\u255F\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u253C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u253C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u253C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u253C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u253C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500╢\n\u2551protected  \u2502   +   \u2502    +    \u2502    +     \u2502    +     \u2502        \u2551\n\u255F\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u253C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u253C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u253C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u253C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u253C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500╢\n\u2551no modifier\u2502   +   \u2502    +    \u2502    +     \u2502          \u2502        \u2551\n\u255F\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u253C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u253C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u253C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u253C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u253C\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500╢\n\u2551private    \u2502   +   \u2502         \u2502          \u2502          \u2502        \u2551\n\u255A\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2567\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2567\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2567\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2567\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2567\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u255D\n+ : accessible                        blank : not accessible```"
);

        tags.put("unsupported",
                """
The java.lang.UnsupportedClassVersionError occurs when the class compiled using a higher version then the currently installed java executable on the system. Meaning, Java is backwards compatible, but not forward. It could happen if you've got two versions of Java installed on your system as well, f.e. javac and java versions don't match. It is often caused by the two versions both set on the PATH environment variable, the first place to look to try and solve the issue.

Class executables are marked with "major.minor" version at bytes 6 and 7, respectfully. This makes Java 8 exactly version 52.0.
{{ user }}
```java
\u2554\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2564\u2550\u2550\u2550\u2564\u2550\u2550\u2550\u2564\u2550\u2550\u2550\u2564\u2550\u2550\u2550\u2564\u2550\u2550\u2564\u2550\u2550\u2564\u2550\u2550\u2564\u2550\u2550\u2564\u2550\u2550\u2564\u2550\u2550\u2564\u2550\u2550\u2564\u2550\u2550\u2564\u2550\u2550\u2564\u2550\u2550\u2557
\u2551 Major Version \u250245 \u250246 \u250247 \u250248 \u250249\u250250\u250251\u250252\u250253\u250254\u250255\u250256\u250257\u250258\u2551
\u255F\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u2500\u253C\u2500\u2500\u2500\u253C\u2500\u2500\u2500\u253C\u2500\u2500\u2500\u253C\u2500\u2500\u2500\u253C\u2500\u2500\u253C\u2500\u2500\u253C\u2500\u2500\u253C\u2500\u2500\u253C\u2500\u2500\u253C\u2500\u2500\u253C\u2500\u2500\u253C\u2500\u2500\u253C\u2500\u2500\u253C\u2500\u2500\u2551
\u2551 Java Version  \u25021.1\u25021.2\u25021.3\u25021.4\u25025 \u25026 \u25027 \u25028 \u25029 \u250210\u250211\u250212\u250213\u250214\u2551
\u255A\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2550\u2567\u2550\u2550\u2550\u2567\u2550\u2550\u2550\u2567\u2550\u2550\u2550\u2567\u2550\u2550\u2550\u2567\u2550\u2550\u2567\u2550\u2550\u2567\u2550\u2550\u2567\u2550\u2550\u2567\u2550\u2550\u2567\u2550\u2550\u2567\u2550\u2550\u2567\u2550\u2550\u2567\u2550\u2550\u2567\u2550\u2550\u255D
```
"""
);

        tags.put("typeparam",
"""
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
"""
);


        tags.put("this",
"""
{{ user }}
`this` is a keyword in Java that references the current instance of the class.

`this` can be passed to other methods to reference itself. An example of this is show here:
```java
public class Foo {
    public Bar getBar() {
        return new Bar(this); //references this instance
    }
}

public class Bar {
    public Foo foo;

    public Bar(Foo object) {
        foo = object;
    }

    public static void main(String[] args) {
        Foo foo = new Foo();
        Bar bar = foo.getBar();
        boolean same = (foo == bar.foo); //true because both reference the same Foo object
    }
}```


`this` can also be used with **variable scope**. Using the variables from the previous example, there is the `foo` *field* in `Bar`, and the `object` *local variable* in the `Bar` constructor. A **field** is a variable that resides in the class itself. A **local variable** is a variable that is created inside of a method. Using `this`, you can now change the name of the Foo `object` in the `Bar` constructor to also be `foo`. When you have a local variable and field with the same name, Java differentiates these using `this`. You can reference the `foo` field with `this.foo` and the local variable with regular `foo`. The updated constructor would then look like
```java
public Bar(Foo foo) {
    this.foo = foo; //this sets the field foo to the value of the local variable foo in the constructor
}
```
"""
);


        tags.put("syntax",
"{{ user }} https://en.wikipedia.org/wiki/Java_syntax"
);

        tags.put("student",
"""
{{ user }}
Github gives students free stuff <https://education.github.com/pack>
AWS gives students free stuff <https://aws.amazon.com/education/awseducate/>
JetBrains gives students free stuff <https://www.jetbrains.com/student/>
All the free stuff <https://github.com/AchoArnold/discount-for-student-dev>
"""
);

        tags.put("ad",
"""
Please do not advertise your ask for help in other help channels{{ user }}. Just wait until someone responds to your initial problem in the original channel you asked in. Asking in multiple channels makes it harder for others to find an open channel.
Also refrain from advertising to pay people for services. This is not the place.
"""
);

        tags.put("git",
"""
Github has both "learn-by-reading" and "learn-by-doing" materials:{{ user }} <https://try.github.io/>
This is a nice visual guide to learning git: <https://learngitbranching.js.org/>
Git tower has a free eBook & video course for learning git: <https://www.git-tower.com/learn/>
"""
);

        tags.put("question",
                """
If you have a question, please ask it in one of our help channels{{ user }}. Make sure to ask in a free help channel, one without an active conversation if it is possible.
Avoid being vague.
State your problem clearly.
State, if anything, what you have already attempted.
Include any code snippets and error messages.
Use `^?share` for a list of sites you can use to share longer bits of code.
"""
);

        tags.put("busychannel",
"""
This channel is **currently in use** by someone else{{ user }}. Please ask your question in an **available channel**, without any active conversation, if possible.
    
Available channels are listed in #open_channel_info. If a previous conversation is without any response for longer than about one hour, it is also fine to ask :thumbsup:
"""
);


        tags.put("ping",
"""
When someone in particular isn't currently helping you{{ user }} then you shouldn't ping them in order to request their help. Be patient when waiting for an answer in the channel you've already posted your question instead. People found helping on TJ server tend to be busy when not looking to provide assistance at the very moment, therefore respecting their valuable time needs to remain your priority if help is what you really need.

If you were helping someone, or vice-versa, and they simply didn't see your message, then pinging them is warranted. Even so, mind that there are people who just prefer not to be pinged.
"""
);

        tags.put("mooc",
                """
MOOC is an introductory Java course created by the University of Helsinki{{ user }}, it is a great way to learn Java from the ground up. It is available in both English and Finnish. https://java-programming.mooc.fi/
"""
);

        tags.put("share",
"""
You can share longer code snippets by posting on:{{ user }}

PasteOfCode: <https://paste.ofcode.org/>
Gist: <https://gist.github.com/>
Pastebin: <https://pastebin.com/>
Hastebin: <https://hastebin.com/>

Please remember to select the appropriate syntax highlighting. It makes your code easier to read!
"""
);


        tags.put("ossu",
"""
Open Source Society University is a collection of free courses that follow the progression of a standard Computer Science degree.
<https://github.com/ossu/computer-science>
"""
);


        client.on(MessageCreateEvent.class).subscribe(e -> {
            Message message = e.getMessage();
            String messageContent = message.getContent();

            if (messageContent.startsWith("^?"))
            {
                Stream<User> mentions = message.getUserMentions().toStream();
                String tag = messageContent.split(" ")[0].substring(2);

                if (tags.containsKey(tag))
                {
                    String user = mentions.map(User::getMention).collect(Collectors.joining(", "));

                    message.getChannel().block().createMessage(tags.get(tag).replace("{{ user }}", user.isEmpty() ? "" : " " + user)).block();
                }
            }
            else if (messageContent.equals("^!"))
                message.getChannel().block().createMessage("All available tags:\n" + String.join(", ", tags.keySet())).block();
        });

        client.on(InteractionCreateEvent.class).subscribe(e -> {
            if (e.getCommandName().equals("tophelpers"))
            {
                e.acknowledge().block();

                Guild guild = e.getInteraction().getGuild().block();

                Optional<ApplicationCommandInteractionOption> limitOption = e.getInteraction()
                        .getCommandInteraction()
                        .getOption("limit");

                if (limitOption.isEmpty())
                    throw new RuntimeException("Unexpected exception");

                int limit = Math.toIntExact(limitOption
                                .get()
                                .getValue()
                                .orElse(APPLICATION_COMMAND_INTERACTION_OPTION_VALUE_LONG_10)
                                .asLong());

                Database.DATABASE.safeUpdate("DELETE FROM messages WHERE timestamp < %d", System.currentTimeMillis() - 2592000000L /* 30 days */);

                Tuple<Statement, ResultSet> query = Database.DATABASE.safeQuery("SELECT user FROM messages");
                Statement statement = query.getA();
                ResultSet result = query.getB();
                CountableMap<Long> messages = new CountableTreeMap<>();

                try
                {
                    while (result.next())
                        messages.count(result.getLong(1));

                    statement.close();
                } catch (SQLException ex)
                {
                    throw new RuntimeException(ex);
                }

                AtomicInteger pos = new AtomicInteger();

                StringBuilder message = messages.entrySet()
                        .stream()
                        .sorted(ENTRY_LONG_VALUE_COMPARATOR)
                        .limit(limit)
                        .map(entry -> Map.entry(
                            guild.getMemberById(Snowflake.of(entry.getKey())).block().getTag(),
                            entry.getValue()
                        ))
                        .map(entry -> new StringBuilder("#")
                                .append(pos.incrementAndGet())
                                .append(" ")
                                .append(entry.getKey())
                                .append(" - ")
                                .append(entry.getValue())
                                .append(" messages in help channels in the last 30 days")
                                .append("\n"))
                        .reduce(StringBuilder::append)
                        .orElse(NO_ENTRIES_STRINGBUILDER);

                e.getInteractionResponse().createFollowupMessage(message.toString()).block();
            }
        });

        executorService.submit(() -> {
            while (!SCANNER.nextLine().equals("stop") && client != null);

            if (client != null) reset();

            Runtime.getRuntime().exit(0);
        });

        ApplicationService applicationService = (restClient = client.getRestClient()).getApplicationService();

        applicationService.getGlobalApplicationCommands(applicationId = restClient.getApplicationId().block()).toStream().forEach(c -> applicationService.deleteGlobalApplicationCommand(applicationId, Long.parseLong(c.id())));
        applicationService.createGlobalApplicationCommand(applicationId, TOP_HELPERS_COMMAND).block();

        client.onDisconnect().block();
    }

    @Override
    public void reset()
    {
        client.logout().block();
        executorService.shutdown();

        try
        {
            Database.DATABASE.disconnect();
        } catch (SQLException thr)
        {
            throw new RuntimeException(thr);
        }

        client = null;
        executorService = null;
    }
}