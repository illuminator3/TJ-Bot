package gg.discord.tj.bot.command;

import discord4j.core.event.domain.message.MessageDeleteEvent;
import gg.discord.tj.bot.domain.BaseEventHandler;
import reactor.core.publisher.Mono;

public class PurgeableCommandHandler extends BaseEventHandler<MessageDeleteEvent> {

    @Override
    public Mono<Void> handleEvent(MessageDeleteEvent event) {
        return Mono.empty();
    }
}
