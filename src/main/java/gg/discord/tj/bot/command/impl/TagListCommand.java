package gg.discord.tj.bot.command.impl;

import gg.discord.tj.bot.app.Application;
import gg.discord.tj.bot.command.Command;
import gg.discord.tj.bot.command.CommandExecutionContext;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TagListCommand
    implements Command
{
    @Override
    public String getName()
    {
        return "taglist";
    }

    @Override
    public Collection<String> getAliasses()
    {
        return List.of("tags");
    }

    @Override
    public void onExecute(CommandExecutionContext context)
    {
        Objects.requireNonNull(context.getMessage().getChannel().block())
                .createMessage("All available tags:\n" + String.join(", ", Application.BOT_INSTANCE
                        .getAvailableTags().keySet().stream()
                        .sorted()
                        .collect(Collectors.toList())))
                .block();
    }
}