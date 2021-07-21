package gg.discord.tj.bot.command.impl;

import gg.discord.tj.bot.app.Application;
import gg.discord.tj.bot.command.Command;
import gg.discord.tj.bot.command.CommandExecutionContext;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HelpCommand
    implements Command
{
    @Override
    public String getName()
    {
        return "help";
    }

    @Override
    public Collection<String> getAliases()
    {
        return List.of("h");
    }

    @Override
    public void onExecute(CommandExecutionContext context)
    {
        Objects.requireNonNull(context.getMessage()
                .getChannel()
                .block())
                .createMessage("""
                        -- Commands
                         """ + Application.BOT_INSTANCE.getCommandHandler().getCommandsAsSet().stream().map(c -> c.getClass().getSimpleName() + " (^" + c.getName() + ")").collect(Collectors.joining("\n"))
                )
                .block();
    }
}