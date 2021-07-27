package gg.discord.tj.bot.domain;

import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.event.domain.message.MessageDeleteEvent;
import gg.discord.tj.bot.service.MessageService;
import lombok.SneakyThrows;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;

public abstract non-sealed class BaseEventHandler<T extends Event> implements EventHandler<T> {
    public static final MessageService MESSAGE_SERVICE = new MessageService();

    @SuppressWarnings("unchecked")
    @SneakyThrows
    @Override
    public Class<T> getEventType() {
        return (Class<T>) Class.forName(((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName());
    }

    public final Mono<Void> processEvent(T event) {
        return executeDefaultActionForEvent(event)
            .flatMap(this::handleEvent);
    }

    private Mono<T> executeDefaultActionForEvent(T event) {
        Mono<T> mono = Mono.just(event);
        if (event instanceof MessageCreateEvent createEvent) {
            mono = Mono.just(createEvent)
                .flatMap(MESSAGE_SERVICE::save)
                .then(mono);
        } else if (event instanceof MessageDeleteEvent deleteEvent) {
            mono = Mono.just(deleteEvent)
                .flatMap(MESSAGE_SERVICE::purgeCommandResponseReference)
                .then(mono);
        }
        return mono;
    }

    public abstract Mono<Void> handleEvent(T event);
}
