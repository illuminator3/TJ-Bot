package gg.discord.tj.bot.core;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.ColumnData;
import com.github.freva.asciitable.HorizontalAlign;
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
import gg.discord.tj.bot.command.impl.*;
import gg.discord.tj.bot.db.Database;
import gg.discord.tj.bot.util.Tuple;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
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
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static gg.discord.tj.bot.util.MessageTemplate.PAINTEXT_MESSAGE_TEMPLATE;

@SuppressWarnings("ConstantConditions")
@RequiredArgsConstructor
@Getter
@Slf4j
public class TJBot
    implements Bot
{
    private static final Pattern HELP_CHANNEL_NAME_PATTERN = Pattern.compile("help|review");
    private static final Comparator<Map.Entry<?, Long>> ENTRY_LONG_VALUE_COMPARATOR = (o1, o2) -> o2.getValue().compareTo(o1.getValue());
    private static final String NO_ENTRIES = "No entries";
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

//        client.on(MessageCreateEvent.class).subscribe(e -> {
//            Message message = e.getMessage();
//
//            if (message.getUserMentions().toStream().anyMatch(u -> u.getId().equals(client.getSelfId())))
//                message.addReaction(ReactionEmoji.of(499232301356679169L, "Pingsock", false)).block();
//        }); TODO: check if the mention wasn't a reply to the bot

        loadTags();

        commandHandler = new CommandHandler();

        commandHandler.init(client);

        Set.of(
            new TagCommand(),
            new TagListCommand(),
            new HelpCommand(),
            new FormatCommand(),
            new SyntaxCommand(),
            new LinesCommand(),
            new ProcessCommand()
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

                Tuple<Statement, ResultSet> query = Database.DATABASE
                        .safeQuery("""
                                WITH TOPHELPERS(user, count) AS (
                                    SELECT user, count(*) FROM messages WHERE guild = %d GROUP BY user ORDER BY count(*) DESC LIMIT %d
                                ) SELECT ROW_NUMBER() OVER(ORDER BY count DESC) as '#', user, count from TOPHELPERS
                                """, guild.getId().asLong(), limit);
                Statement statement = query.getFirst();
                ResultSet result = query.getSecond();
                var topHelpersList = new ArrayList<List<String>>();
                try {
                    while (result.next()) {
                        try
                        {
                            var serialId = String.valueOf(result.getLong("#"));
                            var user = guild.getMemberById(Snowflake.of(result.getLong("user"))).block();
                            var msgCount = String.valueOf(result.getLong("count"));
                            if (!user.isBot()) {
                                topHelpersList.add(List.of(serialId, user.getTag(), msgCount));
                            }
                        } catch (Exception ignored) {
                            log.error(ignored.getMessage(), ignored);
                        }
                    }
                    statement.close();
                } catch (SQLException ex) {
                    throw new RuntimeException(ex);
                }
                var characters = com.github.freva.asciitable.AsciiTable.BASIC_ASCII_NO_DATA_SEPARATORS_NO_OUTSIDE_BORDER;
                var columnData = List.<ColumnData<List<String>>>of(
                        new Column().with(row -> row.get(0)),
                        new Column().header("Name").dataAlign(HorizontalAlign.LEFT).with(row -> row.get(1)),
                        new Column().header("Msg Count(last 30 days)").with(row -> row.get(2))
                );
                var message = String.format(
                        PAINTEXT_MESSAGE_TEMPLATE,
                        topHelpersList.isEmpty() ? NO_ENTRIES : AsciiTable.getTable(characters, topHelpersList, columnData)
                );
                e.getInteractionResponse().createFollowupMessage(message).block();
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