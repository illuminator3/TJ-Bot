package gg.discord.tj.bot.core;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.RestClient;
import discord4j.rest.service.ApplicationService;
import discord4j.rest.util.ApplicationCommandOptionType;
import gg.discord.tj.bot.command.CommandHandler;
import gg.discord.tj.bot.command.impl.HelpCommand;
import gg.discord.tj.bot.command.impl.TagCommand;
import gg.discord.tj.bot.command.impl.TagListCommand;
import gg.discord.tj.bot.db.Database;
import gg.discord.tj.bot.util.CountableMap;
import gg.discord.tj.bot.util.CountableTreeMap;
import gg.discord.tj.bot.util.Tuple;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.file.Path;
import java.security.CodeSource;
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
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

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

    private final Map<String, String> availableTags = new HashMap<>();

    private final String token;

    private GatewayDiscordClient client;
    private ExecutorService executorService;
    private RestClient restClient;
    private long applicationId;
    private CommandHandler commandHandler;

    @SneakyThrows
    @Override
    public void start()
    {
        Database.DATABASE.establishConnection(Path.of("tjdatabase.db").toFile().getCanonicalPath());

        Database.DATABASE.update("""
                CREATE TABLE IF NOT EXISTS messages (
                    user long,
                    timestamp long,
                    guild long
                );
                """);

        executorService = Executors.newCachedThreadPool();
        (client = DiscordClient.create(token)
                .login()
                .block()).on(MessageCreateEvent.class).subscribe(e -> {
                    Optional<Snowflake> guildId = e.getGuildId();
                    Message message = e.getMessage();
                    Optional<Member> member = e.getMember();

                    if (member.isPresent() &&
                            guildId.isPresent() &&
                            message.getContent().matches("^(?![?>]tag free).*$") &&
                            HELP_CHANNEL_NAME_PATTERN.matcher(((TextChannel) message.getChannel().block()).getName()).find()
                    )
                        Database.DATABASE.safeUpdate("INSERT INTO messages (user, timestamp, guild) VALUES (%d, %d, %d)", member.get().getId().asLong(), System.currentTimeMillis(), guildId.get().asLong());
                });

        loadTags();

        commandHandler = new CommandHandler();

        commandHandler.init(client);

        Set.of(
            new TagCommand(),
            new TagListCommand(),
            new HelpCommand()
        ).forEach(commandHandler::registerCommand);

        client.on(InteractionCreateEvent.class).subscribe(e -> {
            ApplicationCommandInteraction commandInteraction = e.getInteraction().getCommandInteraction().orElseThrow();

            if (commandInteraction.getName().orElseThrow().equals("tophelpers"))
            {
                e.acknowledge().block();

                Guild guild = e.getInteraction().getGuild().block();
                Optional<ApplicationCommandInteractionOption> limitOption = commandInteraction.getOption("limit");

                if (limitOption.isEmpty())
                    throw new RuntimeException("Unexpected exception");

                int limit = Math.toIntExact(limitOption
                                .get()
                                .getValue()
                                .orElse(APPLICATION_COMMAND_INTERACTION_OPTION_VALUE_LONG_10)
                                .asLong());

                limit = Math.min(limit, 20);

                Database.DATABASE.safeUpdate("DELETE FROM messages WHERE timestamp < %d", System.currentTimeMillis() - 2592000000L /* 30 days */);

                Tuple<Statement, ResultSet> query = Database.DATABASE.safeQuery("SELECT user FROM messages WHERE guild = %d", guild.getId().asLong());
                Statement statement = query.getFirst();
                ResultSet result = query.getSecond();
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
                        .parallel()
                        .map(entry -> {
                            String tag = "Error#0000";

                            try
                            {
                                Member member = guild.getMemberById(Snowflake.of(entry.getKey())).block();

                                if (member.isBot()) // we don't want bots in our top helper list
                                    return null;

                                tag = member.getTag();
                            } catch (Throwable ignored) {} // I don't know how to fix this atm

                            return Map.entry(
                                tag,
                                entry.getValue()
                            );
                        })
                        .filter(Objects::nonNull)
                        .sequential()
                        .limit(limit)
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

    @SneakyThrows
    private void loadTags()
    {
        CodeSource src = TJBot.class.getProtectionDomain().getCodeSource();

        if (src != null)
        {
            URL jar = src.getLocation();
            ZipInputStream zip = new ZipInputStream(jar.openStream());

            while (true)
            {
                ZipEntry e = zip.getNextEntry();

                if (e == null)
                    break;

                String name = e.getName();

                if (name.matches("tags/.+\\.tag"))
                {
                    String tagName = name.substring("tags/".length()).replace(".tag", "");
                    String content = new BufferedReader(new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream(name))).lines().collect(Collectors.joining("\n"));

                    availableTags.put(tagName, content);
                }
            }
        }
    }
}