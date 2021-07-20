package gg.discord.tj.bot.core;

import com.github.freva.asciitable.AsciiTable;
import com.github.freva.asciitable.Column;
import com.github.freva.asciitable.ColumnData;
import com.github.freva.asciitable.HorizontalAlign;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.ApplicationCommandOptionType;
import gg.discord.tj.bot.command.CommandHandler;
import gg.discord.tj.bot.command.impl.*;
import gg.discord.tj.bot.db.DatabaseManager;
import gg.discord.tj.bot.service.StatisticsService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.CodeSource;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static gg.discord.tj.bot.util.MessageTemplate.PLAINTEXT_MESSAGE_TEMPLATE;

@SuppressWarnings("ConstantConditions")
@RequiredArgsConstructor
@Getter
@Slf4j
public class TJBot
    implements Bot
{
    private static final StatisticsService STATISTICS_SERVICE = new StatisticsService();
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
    private CommandHandler commandHandler;

    @SneakyThrows
    @Override
    public void start()
    {
        loadTags();
        initializeServices();

        client = registerDiscordClient();

        registerClientEvents(client);
        registerCommandHandler(client);

        executorService = Executors.newCachedThreadPool();
        executorService.submit(() ->  {
            while (!SCANNER.nextLine().equals("stop") && client != null) ;

            if (client != null) reset();

            Runtime.getRuntime().exit(0);
        });

        client.onDisconnect().block();
    }

    @Override
    public void reset()
    {
        client.logout().block();
        executorService.shutdown();
        DatabaseManager.INSTANCE.disconnect();

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

    private void initializeServices()
    {
        STATISTICS_SERVICE.init();
    }

    private GatewayDiscordClient registerDiscordClient()
    {
        return DiscordClient.create(token)
            .login()
            .block();
    }

    private void registerClientEvents(GatewayDiscordClient client)
    {
        client.on(MessageCreateEvent.class).subscribe(STATISTICS_SERVICE::save);
        client.on(InteractionCreateEvent.class).subscribe(e ->  {
            ApplicationCommandInteraction commandInteraction = e.getInteraction().getCommandInteraction().orElseThrow();

            if (commandInteraction.getName().orElseThrow().equals("tophelpers"))
            {
                List<List<String>> topNHelpers = STATISTICS_SERVICE.topNHelpers(e);
                Character[] characters = AsciiTable.BASIC_ASCII_NO_DATA_SEPARATORS_NO_OUTSIDE_BORDER;
                List<ColumnData<List<String>>> columnData = List.of(
                    new Column().with(row -> row.get(0)),
                    new Column().header("Name").dataAlign(HorizontalAlign.LEFT).with(row -> row.get(1)),
                    new Column().header("Message Count (in the last 30 days)").with(row -> row.get(2))
                );
                String message = String.format(
                    PLAINTEXT_MESSAGE_TEMPLATE,
                    topNHelpers.isEmpty() ? NO_ENTRIES : AsciiTable.getTable(characters, topNHelpers, columnData)
                );

                e.getInteractionResponse().createFollowupMessage(message).block();
            }
        });
    }

    private void registerCommandHandler(GatewayDiscordClient client)
    {
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
    }
}