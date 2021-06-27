package gg.discord.tj.bot.command;

public interface Command
{
    char getCommandCharacter();
    void onExecute(CommandExecutionContext context);
}