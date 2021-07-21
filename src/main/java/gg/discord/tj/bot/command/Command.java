package gg.discord.tj.bot.command;

import java.util.Collection;

public interface Command
{
    String getName();
    Collection<String> getAliases();
    void onExecute(CommandExecutionContext context);
}