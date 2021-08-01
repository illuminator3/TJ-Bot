package gg.discord.tj.bot.repository;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.Event;
import discord4j.discordjson.json.ApplicationCommandData;
import discord4j.discordjson.json.ApplicationCommandRequest;
import discord4j.rest.service.ApplicationService;
import gg.discord.tj.bot.domain.EventHandler;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public enum DiscordRepository {
    INSTANCE;

    private final AtomicReference<GatewayDiscordClient> discordClientRef = new AtomicReference<>();
    private final AtomicLong applicationId = new AtomicLong();

    public void init(String token) {
        GatewayDiscordClient discordClient = discordClientRef.get();
        if (discordClient == null) {
            DiscordClient.create(token)
                .login()
                .map(gatewayDiscordClient -> discordClientRef.compareAndSet(null, gatewayDiscordClient))
                .block();
        }
        if (applicationId.get() == 0L) {
            applicationId.compareAndSet(0, discordClientRef.get().getRestClient().getApplicationId().block());
        }
    }

    public void reset() {
        discordClientRef.get().logout().block();
        discordClientRef.set(null);
        applicationId.set(0L);
    }

    public Mono<Void> onDisconnect() {
        return discordClientRef.get().onDisconnect();
    }

    public Mono<Void> purgeAllGlobalApplicationCommands() {
        GatewayDiscordClient client = discordClientRef.get();
        ApplicationService applicationService = client.getRestClient().getApplicationService();
        Flux<ApplicationCommandData> globalApplicationCommands = applicationService.getGlobalApplicationCommands(applicationId.get());
        return globalApplicationCommands.flatMap(appCommandData -> applicationService.deleteGlobalApplicationCommand(
            Long.parseLong(appCommandData.applicationId()),
            Long.parseLong(appCommandData.id()))).then();
    }

    public Mono<Long> registerGlobalApplicationCommand(ApplicationCommandRequest applicationCommandRequest) {
        GatewayDiscordClient client = discordClientRef.get();
        ApplicationService applicationService = client.getRestClient().getApplicationService();
        return applicationService.createGlobalApplicationCommand(applicationId.get(), applicationCommandRequest)
            .map(applicationCommandData -> Long.valueOf(applicationCommandData.id()));
    }

    public <T extends Event> Mono<Void> registerEventHandlers(List<EventHandler<T>> eventHandlers) {
        GatewayDiscordClient client = discordClientRef.get();
        return Mono.just(eventHandlers)
            .flatMapMany(Flux::fromIterable)
            .flatMap(eventHandler -> client.on(eventHandler.getEventType())
//                .log(String.format("(%s) %s", eventHandler.getEventType().getSimpleName(), eventHandler.getClass().getSimpleName()))
                    .flatMap(eventHandler::processEvent)
                    .onErrorResume(eventHandler::handleError)
            ).then();
    }
}
