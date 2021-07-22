package gg.discord.tj.bot.service;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.command.ApplicationCommandInteraction;
import discord4j.core.object.command.ApplicationCommandInteractionOption;
import discord4j.core.object.command.ApplicationCommandInteractionOptionValue;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.TextChannel;
import discord4j.rest.util.ApplicationCommandOptionType;
import gg.discord.tj.bot.repository.StatisticsRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import reactor.core.publisher.SynchronousSink;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public final class StatisticsService
{
    private final StatisticsRepository repository = StatisticsRepository.INSTANCE;
    private static final Pattern HELP_CHANNEL_NAME_PATTERN = Pattern.compile("help|review");

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

    public int save(MessageCreateEvent e)
    {
        int rowCount = 0;
        Optional<Snowflake> guildId = e.getGuildId();
        Message message = e.getMessage();
        Optional<Member> member = e.getMember();

        if (member.isPresent() &&
            !member.get().isBot() &&
            guildId.isPresent() &&
            message.getContent().matches("^(?![?>]tag free).*$") &&
            HELP_CHANNEL_NAME_PATTERN.matcher(((TextChannel) message.getChannel().block()).getName()).find()
        )
        {
            try
            {
                rowCount = repository.save(guildId.get().asLong(), member.get().getId().asLong());

                log.debug("{} message event saved for [GUILD={}, USER={}]", rowCount, guildId.get().asLong(), member.get().getId().asLong());
            } catch (SQLException throwables)
            {
                log.error(throwables.getMessage(), throwables);
            }
        }
        return rowCount;
    }

    public Mono<List<List<String>>> topNHelpers(InteractionCreateEvent e)
    {
        ApplicationCommandInteraction commandInteraction = e.getInteraction().getCommandInteraction().orElseThrow();

        Guild guild = e.getInteraction().getGuild().block();
        Optional<ApplicationCommandInteractionOption> limitOption = commandInteraction.getOption("limit");

        int limit = 10;
        if (limitOption.isPresent()) {
            Optional<ApplicationCommandInteractionOptionValue> optionValue = limitOption.get().getValue();
            if(optionValue.isPresent()) {
               limit = Math.toIntExact(optionValue.get().asLong());
            }
        }

        limit = Math.min(limit, 20);

        Function<List<List<Long>>, List<List<String>>> enhanceDataFrame = df -> {
            List<List<String>> collect = df.stream()
                .map(row -> {
                        String tag = null;

                        try {
                            tag = guild.getMemberById(Snowflake.of(row.get(1))).block().getTag();
                        } catch (Throwable ignored) {
                        }

                        return Arrays.asList(String.valueOf(row.get(0)), tag, String.valueOf(row.get(2)));
                    }
                ).filter(l -> l.get(1) != null).collect(Collectors.toList());
            return collect;
        };

        int finalLimit = limit;
        return e.getInteraction().getGuild()
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
            }).map(enhanceDataFrame::apply);
    }
}
