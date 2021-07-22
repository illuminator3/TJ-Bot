package gg.discord.tj.bot.service;

import discord4j.core.event.domain.Event;
import discord4j.discordjson.json.ApplicationCommandRequest;
import gg.discord.tj.bot.domain.EventHandler;
import gg.discord.tj.bot.repository.DiscordRepository;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

import java.util.List;

@Slf4j
public final class DiscordService {
    private final DiscordRepository discordRepository = DiscordRepository.INSTANCE;

    public void init(String token) {
        discordRepository.init(token);
    }

    public Mono<Long> cleanRegisterGlobalApplicationCommand(ApplicationCommandRequest applicationCommandRequest) {
        return discordRepository.purgeAllGlobalApplicationCommands()
            .then(discordRepository.registerGlobalApplicationCommand(applicationCommandRequest))
            .doOnSubscribe(subscription -> log.info("Global application commands subscribed: {}", subscription));
    }

    public <T extends Event> Mono<Void> registerEventHandlers(List<EventHandler<T>> eventHandlers) {
        return discordRepository.registerEventHandlers(eventHandlers)
            .doOnSubscribe(subscription -> log.info("Event handlers subscribed: {}", subscription));
    }
}
