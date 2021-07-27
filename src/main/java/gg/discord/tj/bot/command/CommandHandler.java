package gg.discord.tj.bot.command;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import gg.discord.tj.bot.domain.BaseEventHandler;
import gg.discord.tj.bot.repository.CommandRepository;
import lombok.Getter;
import reactor.core.publisher.Mono;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
public class CommandHandler extends BaseEventHandler<MessageCreateEvent> {
    private final static Pattern COMMAND_CAPTURE_PATTERN = Pattern.compile("^(\\^([a-z]+)).*");
    private final static int COMMAND_WITH_PREFIX_GROUP = 1;
    private final static int COMMAND_NAME_GROUP = 2;

    private final static CommandRepository COMMAND_REPOSITORY = CommandRepository.INSTANCE;

    public Mono<Void> handleEvent(MessageCreateEvent event)
    {
        Mono<Void> eventReturn = Mono.empty();
        Message message = event.getMessage();
        String content = message.getContent();
        Matcher commandMatcher = COMMAND_CAPTURE_PATTERN.matcher(content);

        if (commandMatcher.matches()) {
            String commandWithPrefix = commandMatcher.group(COMMAND_WITH_PREFIX_GROUP);
            String commandName = commandMatcher.group(COMMAND_NAME_GROUP);

            eventReturn = COMMAND_REPOSITORY.retrieveCommand(commandName)
                .onExecute(new CommandExecutionContext(message, content.substring(commandWithPrefix.length()).trim()));
        }

        return eventReturn;
    }
}
