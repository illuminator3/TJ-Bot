package gg.discord.tj.bot.service;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import gg.discord.tj.bot.repository.StatisticsRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;

@Slf4j
public final class StatisticsService
{
    private final StatisticsRepository repository = StatisticsRepository.INSTANCE;
    private final static Pattern HELP_CHANNEL_NAME_PATTERN = Pattern.compile("help|review");
    private final static String TAG_FREE_MESSAGE_PATTERN = "^(?![?>]tag free).*$";

    public void init()
    {
        try
        {
            repository.init();
        } catch (SQLException throwables)
        {
            log.error(throwables.getMessage(), throwables);
        }
    }

    public Mono<Integer> save(MessageCreateEvent event)
    {
        Optional<Snowflake> guildId = event.getGuildId();
        Message message = event.getMessage();
        Optional<Member> member = event.getMember();
        return message.getChannel()
            .handle((channel, sink) -> {
                if (guildId.isPresent() &&
                    member.isPresent() &&
                    !member.get().isBot() &&
                    message.getContent().matches(TAG_FREE_MESSAGE_PATTERN) &&
                    HELP_CHANNEL_NAME_PATTERN.matcher(((TextChannel) channel).getName()).find()) {
                    try {
                        int rowCount = repository.save(guildId.get().asLong(), member.get().getId().asLong());
                        log.debug("{} message event saved for [GUILD={}, USER={}]", rowCount, guildId.get().asLong(), member.get().getId().asLong());
                        sink.next(rowCount);
                    } catch (SQLException throwables) {
                        log.error(throwables.getMessage(), throwables);
                        sink.error(throwables);
                    }
                } else {
                    sink.next(0);
                }
            });
    }

    public Mono<List<List<String>>> topNHelpers(InteractionCreateEvent event)
    {
        ApplicationCommandInteraction commandInteraction = event.getInteraction().getCommandInteraction().orElseThrow();
        Optional<ApplicationCommandInteractionOption> limitOption = commandInteraction.getOption("limit");

        int limit = 10;
        if (limitOption.isPresent()) {
            Optional<ApplicationCommandInteractionOptionValue> optionValue = limitOption.get().getValue();
            if(optionValue.isPresent()) {
               limit = Math.toIntExact(optionValue.get().asLong());
            }
        }

        limit = Math.min(limit, 20);

        Function<List<List<Long>>, Flux<List<String>>> enhanceDataFrame = df -> Flux.concat(df.stream()
            .map(row ->
                event.getInteraction().getGuild()
                    .flatMap(guild -> guild.getMemberById(Snowflake.of(row.get(1))))
                    .map(member -> member == null ? "[UNKNOWN MEMBER]" : member.getTag())
                    .map(username -> List.of(String.valueOf(row.get(0)), username, String.valueOf(row.get(2))))
            ).toList());

        int finalLimit = limit;
        return event.getInteraction().getGuild()
            .map(guild1 -> guild1.getId().asLong())
            .handle((BiConsumer<Long, SynchronousSink<List<List<Long>>>>) (guildId, sink) -> {
                try {
                    int rowCount = repository.purge(System.currentTimeMillis() - 2592000000L /* 30 days */);
                    log.debug("{} rows deleted from table MESSAGES as a part of history cleanup", rowCount);
                    List<List<Long>> topNHelpersForGuild = repository.topNHelpersForGuild(guildId, finalLimit);
                    sink.next(topNHelpersForGuild);
                } catch (SQLException throwables) {
                    sink.error(throwables);
                }
            }).map(enhanceDataFrame)
            .flatMap(Flux::collectList);
    }
}
