package gg.discord.tj.bot.domain;

import discord4j.core.event.domain.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Mono;

public sealed interface EventHandler<T extends Event> permits BaseEventHandler {
    Logger log = LoggerFactory.getLogger(EventHandler.class);

    Class<T> getEventType();
    Mono<Void> processEvent(T event);

    default Mono<Void> handleError(Throwable error) {
        log.error("Unable to register handler for event type: " + getEventType().getSimpleName(), error);
        return Mono.empty();
    }
}
