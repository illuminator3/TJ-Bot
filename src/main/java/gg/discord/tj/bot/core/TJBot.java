package gg.discord.tj.bot.core;

import discord4j.common.util.Snowflake;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.InteractionCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.discordjson.json.ApplicationCommandOptionData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.discordjson.json.MessageData;
import discord4j.rest.RestClient;
import discord4j.rest.util.ApplicationCommandOptionType;
import gg.discord.tj.bot.util.CountableMap;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

@SuppressWarnings("ConstantConditions")
public class TJBot
{
    public void start()
        throws Throwable
    {
        ExecutorService executors = Executors.newCachedThreadPool();

        String token = "ODQ5NjgxMzU3NjA1MDQ0MzI1.YLetbQ.rfYZpL404hIMCDiznam-lhhjRIc";
        GatewayDiscordClient client = DiscordClient.create(token)
                .login()
                .block();
        ApplicationCommandRequest tophelpersCommand = ApplicationCommandRequest.builder()
                .name("tophelpers")
                .description("View the top helpers of last month")
                .addOption(ApplicationCommandOptionData.builder()
                        .name("limit")
                        .description("The amount of top helpers")
                        .required(true)
                        .type(ApplicationCommandOptionType.INTEGER.getValue())
                        .build())
                .build();

        executors.submit(() -> {
            Scanner sc = new Scanner(System.in);

            while (!sc.nextLine().equals("stop"));

            client.logout().block();

            Runtime.getRuntime().exit(0);
        });

        RestClient restClient = client.getRestClient();

        restClient.getApplicationService()
                .createGlobalApplicationCommand(restClient.getApplicationId().block(), tophelpersCommand);

        client.on(InteractionCreateEvent.class).subscribe(e -> {
            if (e.getCommandName().equals("tophelpers"))
            {
                e.acknowledge().block();
                response(e, Math.toIntExact(e.getInteraction().getCommandInteraction().getOption("limit").get().getValue().get().asLong())).block();
            }
        });

        client.onDisconnect().block();
    }

    public void reset()
    {
    }

    private Mono<MessageData> response(InteractionCreateEvent e, int limit)
    {
        return e.getInteractionResponse().createFollowupMessage(gatherData(e, limit).toString());
    }

    private StringBuilder gatherData(InteractionCreateEvent e, int limit)
    {
        CountableMap<Snowflake> messages = new CountableMap<>();
        Guild guild = e.getInteraction().getGuild().block();

        guild
            .getChannels()
            .ofType(TextChannel.class)
            .filter(t -> t.getName().matches(".*(help|review).*")) // possibly compile the pattern in a constant
            .map(c ->
                c.getMessagesAfter(
                    Snowflake.of(Instant.now().minus(Duration.ofDays(30)))
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
                .sorted((o1, o2) -> o2.getValue().compareTo(o1.getValue()))
                .limit(limit)
                .map(entry -> {
                    Snowflake snowflake = entry.getKey();
                    long amount = entry.getValue();
                    Member member = guild.getMemberById(snowflake).block();

                    return new StringBuilder().append("#")
                      .append(pos.incrementAndGet())
                      .append(" ")
                      .append(member.getDisplayName())
                      .append("#")
                      .append(member.getDiscriminator())
                      .append(" - ")
                      .append(amount)
                      .append(" messages in help channels in the last 30 days")
                      .append("\n");
                })
                .reduce(StringBuilder::append)
                .orElseGet(() -> new StringBuilder("No entries"));
    }
}