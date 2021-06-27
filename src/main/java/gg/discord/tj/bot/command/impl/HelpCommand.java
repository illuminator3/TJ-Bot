package gg.discord.tj.bot.command.impl;

import gg.discord.tj.bot.app.Application;
import gg.discord.tj.bot.command.Command;
import gg.discord.tj.bot.command.CommandExecutionContext;

import java.util.Objects;
import java.util.stream.Collectors;

public class HelpCommand
    implements Command
{
    @Override
    public char getCommandCharacter()
    {
        return '$';
    }

    @Override
    public void onExecute(CommandExecutionContext context)
    {
        Objects.requireNonNull(context.getMessage()
                .getChannel()
                .block())
                .createMessage("""
                        -- Commands
                        """ + Application.BOT_INSTANCE.getCommandHandler().getCommands().stream().map(c -> c.getClass().getSimpleName() + " (^" + c.getCommandCharacter() + ")").collect(Collectors.joining("\n"))
                )
                .block();
    }
}