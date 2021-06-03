package gg.discord.tj.bot.core;

import discord4j.core.GatewayDiscordClient;
import discord4j.rest.RestClient;

import java.util.concurrent.ExecutorService;

public interface Bot
{
    void start() throws Throwable;
    void reset();
    GatewayDiscordClient getClient();
    RestClient getRestClient();
    ExecutorService getExecutorService();
}