package gg.discord.tj.bot.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import gg.discord.tj.bot.domain.BaseEventHandler;
import reactor.core.publisher.Mono;

public class NotACommand extends BaseEventHandler<MessageCreateEvent> {
    @Override
    public Mono<Void> handleEvent(MessageCreateEvent event) {
        return Mono.empty();
    }
}
