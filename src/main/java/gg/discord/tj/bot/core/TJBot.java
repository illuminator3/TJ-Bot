package gg.discord.tj.bot.core;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.InteractionCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.json.MessageData;
import discord4j.rest.RestClient;
import discord4j.rest.util.ApplicationCommandOptionType;
import gg.discord.tj.bot.util.CountableMap;
import gg.discord.tj.bot.util.CountableTreeMap;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;

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

    @Override
    public void start()
    {
        executorService = Executors.newCachedThreadPool();
        (client = DiscordClient.create(token)
                    .login()
                    .block()).on(InteractionCreateEvent.class).subscribe(e -> {
                        if (e.getCommandName().equals("tophelpers"))
                        {
                            e.acknowledge().block();
                            response(e, Math.toIntExact(e.getInteraction()
                                    .getCommandInteraction()
                                    .getOption("limit")
                                    .get()
                                    .getValue()
                                    .orElse(APPLICATION_COMMAND_INTERACTION_OPTION_VALUE_LONG_10)
                                    .asLong())).block();
                        }
                    });


        executorService.submit(() -> {
            while (!SCANNER.nextLine().equals("stop") && client != null);

            if (client != null) reset();

            Runtime.getRuntime().exit(0);
        });

        (restClient = client.getRestClient()).getApplicationService()
                .createGlobalApplicationCommand(applicationId = restClient.getApplicationId().block(), TOP_HELPERS_COMMAND);

        client.onDisconnect().block();
    }

    @Override
    public void reset()
    {
        client.logout().block();
        executorService.shutdown();

        client = null;
        executorService = null;
    }

    private Mono<MessageData> response(InteractionCreateEvent e, int limit)
    {
        return e.getInteractionResponse()
                .createFollowupMessage(gatherData(e, limit).toString());
    }

    private StringBuilder gatherData(InteractionCreateEvent e, int limit)
    {
        CountableMap<Snowflake> messages = new CountableTreeMap<>();
        Guild guild = e.getInteraction().getGuild().block();

        guild
            .getChannels()
            .ofType(TextChannel.class)
            .filter(tc -> HELP_CHANNEL_NAME_PATTERN.matcher(tc.getName()).find())
            .map(tc ->
                tc.getMessagesAfter(
                    Snowflake.of(Instant.now().minus(DURATION_30D))
                ))
            .collectList()
            .block()
            .forEach(f -> f.collectList()
                .block()
                .stream()
                .map(Message::getAuthor)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(User::getId)
                .forEach(messages::count));

        AtomicInteger pos = new AtomicInteger();

        return messages.entrySet()
                .stream()
                .sorted(ENTRY_LONG_VALUE_COMPARATOR)
                .limit(limit)
                .map(entry -> Map.entry(
                    guild.getMemberById(entry.getKey()).block().getTag(),
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
                .orElseGet(() -> NO_ENTRIES_STRINGBUILDER);
    }
}