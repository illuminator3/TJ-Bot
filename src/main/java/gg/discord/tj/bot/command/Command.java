package gg.discord.tj.bot.command;

import java.util.Collection;

public interface Command
{
    String getName();
    Collection<String> getAliasses();
    void onExecute(CommandExecutionContext context);
}