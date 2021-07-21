package gg.discord.tj.bot.core;

import gg.discord.tj.bot.command.CommandHandler;

import java.util.Map;

public interface Bot
{
    void start();
    void reset();
    Map<String, String> getAvailableTags();
    CommandHandler getCommandHandler();
}