package gg.discord.tj.bot.command;

import reactor.core.publisher.Mono;

import java.util.Collection;

public interface Command
{
    String getName();
    Collection<String> getAliases();
    String getDescription();
    Mono<Void> onExecute(CommandExecutionContext context);
}
