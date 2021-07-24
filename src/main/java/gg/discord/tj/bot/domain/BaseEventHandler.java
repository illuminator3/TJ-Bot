package gg.discord.tj.bot.domain;

import discord4j.core.event.domain.Event;
import discord4j.core.event.domain.message.MessageCreateEvent;
import gg.discord.tj.bot.service.StatisticsService;
import lombok.SneakyThrows;
import reactor.core.publisher.Mono;

import java.lang.reflect.ParameterizedType;

public abstract non-sealed class BaseEventHandler<T extends Event> implements EventHandler<T> {
    public static final StatisticsService STATISTICS_SERVICE = new StatisticsService();

    @SuppressWarnings("unchecked")
    @SneakyThrows
    @Override
    public Class<T> getEventType() {
        return (Class<T>) Class.forName(((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0].getTypeName());
    }

    public final Mono<Void> processEvent(T event) {
        return saveIfMessageCreateEvent(event)
            .flatMap(this::handleEvent);
    }

    private Mono<T> saveIfMessageCreateEvent(T event) {
        Mono<T> mono = Mono.just(event);
        if (event instanceof MessageCreateEvent createEvent) {
            mono = Mono.just(createEvent)
                .flatMap(STATISTICS_SERVICE::save)
                .then(mono);
        }
        return mono;
    }

    public abstract Mono<Void> handleEvent(T event);
}
