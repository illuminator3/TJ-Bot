package gg.discord.tj.bot.command.impl;

import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import gg.discord.tj.bot.app.Application;
import gg.discord.tj.bot.command.Command;
import gg.discord.tj.bot.command.CommandExecutionContext;
import gg.discord.tj.bot.service.MessageService;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TagCommand implements Command {
    public static final MessageService MESSAGE_SERVICE = new MessageService();

    @Override
    public String getName()
    {
        return "tag";
    }

    @Override
    public Collection<String> getAliases()
    {
        return List.of("t", "?");
    }

    @Override
    public String getDescription() {
        return """
            Prints relevant description against issued tag. To view the list of available tags use ^taglist.
            eg. ^tag ++
            """;
    }

    @Override
    public Mono<Void> onExecute(CommandExecutionContext context)
    {
        Map<String, String> tags = Application.BOT_INSTANCE.getAvailableTags();
        String users = context.message().getUserMentions()
            .toStream()
            .map(User::getMention)
            .collect(Collectors.joining(", "));
        Message message = context.message();
        return message
            .getChannel()
            .flatMap(channel -> channel == null ?
                Mono.empty() :
                channel.createMessage(tags.get(context.commandContent().split(" ")[0])
                .replace("{{ user }}", users.isEmpty() ? "" : " " + users))
                    .flatMap(responseMessage -> MESSAGE_SERVICE.setPurgableCommandResponseReference(message.getId().asLong(), responseMessage))
            )
            .then();
    }
}
