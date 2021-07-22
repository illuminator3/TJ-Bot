package gg.discord.tj.bot.command;

import com.github.freva.asciitable.HorizontalAlign;
import discord4j.core.event.domain.interaction.InteractionCreateEvent;
import gg.discord.tj.bot.domain.BaseEventHandler;
import gg.discord.tj.bot.util.PresentationUtils;
import reactor.core.publisher.Mono;

import static gg.discord.tj.bot.util.MessageTemplate.PLAINTEXT_MESSAGE_TEMPLATE;

public final class GlobalTopHelpersApplicationCommand extends BaseEventHandler<InteractionCreateEvent> {
    private static final String NO_ENTRIES = "No entries";

    @Override
    public Mono<Void> handleEvent(InteractionCreateEvent event) {
        return generateResponse(event, getCommandInterationName(event))
            .flatMap(s -> event.acknowledge().then(event.getInteractionResponse().createFollowupMessage(s)))
            .then();
    }

    private Mono<String> generateResponse(InteractionCreateEvent e, String commandInterationName) {
        return switch (getCommandInterationName(e)) {
            case "tophelpers" ->
                STATISTICS_SERVICE.topNHelpers(e)
                .map(topNHelpers -> String.format(
                        PLAINTEXT_MESSAGE_TEMPLATE,
                        topNHelpers.isEmpty() ?
                            NO_ENTRIES :
                            PresentationUtils.dataFrameToAsciiTable(topNHelpers,
                                new String[]{"#", "Name", "Message Count (in the last 30 days)"},
                                new HorizontalAlign[]{HorizontalAlign.RIGHT, HorizontalAlign.LEFT, HorizontalAlign.RIGHT}
                            )
                    )
                );
            default -> throw new IllegalStateException("Unexpected value: " + commandInterationName);
        };
    }

    private String getCommandInterationName(InteractionCreateEvent event) {
        return event.getInteraction().getCommandInteraction()
            .orElseThrow()
            .getName()
            .orElse("");
    }
}
