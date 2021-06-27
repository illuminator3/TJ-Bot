package gg.discord.tj.bot.core;

import discord4j.core.GatewayDiscordClient;
import discord4j.rest.RestClient;
import gg.discord.tj.bot.command.CommandHandler;

import java.util.Map;
import java.util.concurrent.ExecutorService;

public interface Bot
{
    void start();
    void reset();
    GatewayDiscordClient getClient();
    RestClient getRestClient();
    ExecutorService getExecutorService();
    Map<String, String> getAvailableTags();
    CommandHandler getCommandHandler();
}