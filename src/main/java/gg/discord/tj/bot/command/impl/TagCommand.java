package gg.discord.tj.bot.command.impl;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import gg.discord.tj.bot.app.Application;
import gg.discord.tj.bot.command.Command;
import gg.discord.tj.bot.command.CommandExecutionContext;

import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TagCommand
    implements Command
{
    @Override
    public char getCommandCharacter()
    {
        return '?';
    }

    @Override
    public void onExecute(CommandExecutionContext context)
    {
        Message message = context.getMessage();
        String messageContent = message.getContent();
        Stream<User> mentions = message.getUserMentions().toStream();
        String tag = messageContent.split(" ")[0].substring(2);
        Map<String, String> tags = Application.BOT_INSTANCE.getAvailableTags();

        if (tags.containsKey(tag))
        {
            String users = mentions.map(User::getMention).collect(Collectors.joining(", "));

            Objects.requireNonNull(message.getChannel().block())
                    .createMessage(tags.get(tag)
                            .replace("{{ user }}", users.isEmpty() ? "" : " " + users))
                    .block();
        }
    }
}