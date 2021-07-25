package gg.discord.tj.bot.command.impl;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import gg.discord.tj.bot.app.Application;
import gg.discord.tj.bot.command.Command;
import gg.discord.tj.bot.command.CommandExecutionContext;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TagCommand
    implements Command {
    public static final Map<Snowflake, Message> TAG_MESSAGE_CACHE = new HashMap<>();

    @Override
    public String getName() {
        return "tag";
    }

    @Override
    public Collection<String> getAliases() {
        return List.of("t", "?");
    }

    @Override
    public void onExecute(CommandExecutionContext context) {
        Message message = context.getMessage();
        Stream<User> mentions = message.getUserMentions().toStream();
        String tag = context.getContent().split(" ")[0];
        Map<String, String> tags = Application.BOT_INSTANCE.getAvailableTags();

        if (tags.containsKey(tag)) {
            String users = mentions.map(User::getMention).collect(Collectors.joining(", "));

            Message msg = Objects.requireNonNull(message.getChannel().block())
                    .createMessage(tags.get(tag)
                            .replace("{{ user }}", users.isEmpty() ? "" : " " + users))
                    .block();

            TAG_MESSAGE_CACHE.put(message.getId(), msg);
        }
    }
}