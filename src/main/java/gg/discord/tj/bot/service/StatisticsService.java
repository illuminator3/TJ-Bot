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
import gg.discord.tj.bot.db.StatisticsRepository;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
public final class StatisticsService
{
    private final StatisticsRepository repository = StatisticsRepository.INSTANCE;
    private static final Pattern HELP_CHANNEL_NAME_PATTERN = Pattern.compile("help|review");
    private static final ApplicationCommandInteractionOptionValue APPLICATION_COMMAND_INTERACTION_OPTION_VALUE_LONG_10 =
            new ApplicationCommandInteractionOptionValue(null, null, ApplicationCommandOptionType.INTEGER.getValue(), "10");

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

    public void save(MessageCreateEvent e)
    {
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
                int rowCount = repository.save(guildId.get().asLong(), member.get().getId().asLong());

                log.debug("{} message event saved for [GUILD={}, USER={}]", rowCount, guildId.get().asLong(), member.get().getId().asLong());
            } catch (SQLException throwables)
            {
                log.error(throwables.getMessage(), throwables);
            }
        }
    }

    public List<List<String>> topNHelpers(InteractionCreateEvent e)
    {
        List<List<String>> topNHelpers = new ArrayList<>();
        ApplicationCommandInteraction commandInteraction = e.getInteraction().getCommandInteraction().orElseThrow();

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

        Function<List<List<Long>>, List<List<String>>> enhanceDataFrame = df ->
            df.stream()
                .map(row ->
                    List.of(String.valueOf(row.get(0)),
                        guild.getMemberById(Snowflake.of(row.get(1))).block().getTag(), // FIXME guild.getMemberById(Snowflake.of(row.get(1))) can throw an exception
                        String.valueOf(row.get(2)))
                ).collect(Collectors.toList());
        try
        {
            int rowCount = repository.purge(System.currentTimeMillis() - 2592000000L /* 30 days */);

            log.debug("{} rows deleted from table MESSAGES as a part of history cleanup", rowCount);

            List<List<Long>> topNHelpersForGuild = repository.topNHelpersForGuild(guild.getId().asLong(), limit);

            topNHelpers = enhanceDataFrame.apply(topNHelpersForGuild);
        } catch (SQLException throwables)
        {
            log.error(throwables.getMessage(), throwables);
        }

        return topNHelpers;
    }
}