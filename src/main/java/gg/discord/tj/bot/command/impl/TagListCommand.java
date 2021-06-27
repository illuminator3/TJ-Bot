package gg.discord.tj.bot.command.impl;

import gg.discord.tj.bot.app.Application;
import gg.discord.tj.bot.command.Command;
import gg.discord.tj.bot.command.CommandExecutionContext;

import java.util.Objects;

public class TagListCommand
    implements Command
{
    @Override
    public char getCommandCharacter()
    {
        return '!';
    }

    @Override
    public void onExecute(CommandExecutionContext context)
    {
        Objects.requireNonNull(context.getMessage().getChannel().block())
                .createMessage("All available tags:\n" + String.join(", ", Application.BOT_INSTANCE.getAvailableTags().keySet()))
                .block();
    }
}