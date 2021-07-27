package gg.discord.tj.bot.core;

import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageDeleteEvent;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.util.ApplicationCommandOptionType;
import gg.discord.tj.bot.command.CommandHandler;
import gg.discord.tj.bot.command.GlobalTopHelpersApplicationCommand;
import gg.discord.tj.bot.command.PurgeableCommandHandler;
import gg.discord.tj.bot.command.impl.*;
import gg.discord.tj.bot.domain.EventHandler;
import gg.discord.tj.bot.repository.CommandRepository;
import gg.discord.tj.bot.repository.DatabaseManager;
import gg.discord.tj.bot.service.DiscordService;
import gg.discord.tj.bot.service.MessageService;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URL;
import java.security.CodeSource;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@SuppressWarnings("ConstantConditions")
@RequiredArgsConstructor
@Getter
@Slf4j
public class TJBot implements Bot {
    private static final DiscordService DISCORD_SERVICE = new DiscordService();
    private static final MessageService MESSAGE_SERVICE = new MessageService();
    private final static CommandRepository COMMAND_REPOSITORY = CommandRepository.INSTANCE;

    private static final Duration DURATION_30D = Duration.ofDays(30);
    private static final Scanner SCANNER = new Scanner(System.in);
    private static final ApplicationCommandRequest TOP_HELPERS_COMMAND = ApplicationCommandRequest.builder()
            .name("tophelpers")
            .description("View the top helpers of last month")
            .addOption(ApplicationCommandOptionData.builder()
                    .name("limit")
                    .description("The amount of top helpers")
                    .required(false)
                    .type(ApplicationCommandOptionType.INTEGER.getValue())
                    .build())
            .build();

    private final Map<String, String> availableTags = new HashMap<>();

    private final String token;

    @SneakyThrows
    @Override
    public void start() {
        Set.of(
            new TagCommand(),
            new TagListCommand(),
            new HelpCommand(),
            new FormatCommand(),
            new SyntaxCommand(),
            new LinesCommand(),
            new ProcessCommand(),
            new UploadCommand()
        ).forEach(COMMAND_REPOSITORY::registerCommand);

        List<EventHandler<MessageCreateEvent>> msgCreateEventHandlers = List.of(
            new CommandHandler()
        );
        List<EventHandler<MessageDeleteEvent>> msgDeleteEventHandlers = List.of(
            new PurgeableCommandHandler()
        );
        List<EventHandler<InteractionCreateEvent>> interactionCreateEventHandlers = List.of(
            new GlobalTopHelpersApplicationCommand()
        );

        loadTags();
        initializeServices();

        DISCORD_SERVICE.cleanRegisterGlobalApplicationCommand(TOP_HELPERS_COMMAND)
            .subscribe(appId -> {
                DISCORD_SERVICE.registerEventHandlers(interactionCreateEventHandlers)
                    .subscribe(unused -> log.info("Global command handlers initialized"));
                DISCORD_SERVICE.registerEventHandlers(msgCreateEventHandlers)
                    .subscribe(unused -> log.info("Message create handlers initialized"));
                DISCORD_SERVICE.registerEventHandlers(msgDeleteEventHandlers)
                    .subscribe(unused -> log.info("Message delete handlers initialized"));
            });

        DISCORD_SERVICE.onDisconnect().block();
    }

    @Override
    public void reset() {
        DISCORD_SERVICE.reset();
        DatabaseManager.INSTANCE.disconnect();
    }

    @SneakyThrows
    private void loadTags() {
        CodeSource src = TJBot.class.getProtectionDomain().getCodeSource();

        if (src != null) {
            URL jar = src.getLocation();
            ZipInputStream zip = new ZipInputStream(jar.openStream());

            while (true) {
                ZipEntry e = zip.getNextEntry();

                if (e == null)
                    break;

                String name = e.getName();

                if (name.matches("tags/.+\\.tag")) {
                    String tagName = name.substring("tags/".length()).replace(".tag", "");
                    String content = new BufferedReader(new InputStreamReader(ClassLoader.getSystemClassLoader().getResourceAsStream(name))).lines().collect(Collectors.joining("\n"));

                    availableTags.put(tagName, content);
                }
            }
        }
    }

    private void initializeServices() {
        DISCORD_SERVICE.init(token);
        MESSAGE_SERVICE.init();

        registerShutdownService();
    }

    private void registerShutdownService() {
        Executors.newFixedThreadPool(1).submit(() -> {
            while (!SCANNER.nextLine().equals("stop")) ;

            reset();

            Runtime.getRuntime().exit(0);
        });
    }
}
