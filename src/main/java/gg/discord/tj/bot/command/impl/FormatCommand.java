package gg.discord.tj.bot.command.impl;

import discord4j.core.object.MessageReference;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import gg.discord.tj.bot.command.Command;
import gg.discord.tj.bot.command.CommandExecutionContext;
import gg.discord.tj.bot.util.Hastebin;
import gg.discord.tj.bot.util.JavaFormatUtils;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import static gg.discord.tj.bot.util.Constants.DISCORD_MAX_MESSAGE_LENGTH;
import static gg.discord.tj.bot.util.MessageTemplate.JAVA_MESSAGE_TEMPLATE;

public class FormatCommand implements Command {
    @Override
    public String getName() {
        return "format";
    }

    @Override
    public Collection<String> getAliases() {
        return List.of("f");
    }

    @Override
    public String getDescription() {
        return "Formats/prettifies source code when issued against a message.";
    }

    @Override
    public Mono<Void> onExecute(CommandExecutionContext context) {
        Message message = context.message();
        Optional<MessageReference> referenceOpt = message.getMessageReference();

        return context.message()
            .getChannel()
            .flatMap(channel -> channel == null ? // 1. Check if channel is empty. May be it was deleted
                Mono.empty() :                    // 1. TRUE: Nothing to action
                referenceOpt.isEmpty() ?          // 1. FALSE: 2. Check if the current msg has a msg reference ie. if this was in reply to an earlier msg.
                    channel.createMessage("This command works by replying to a message containing unformatted code") : // 2. TRUE: Reply back that it wont work.
                    channel.getMessageById(referenceOpt.get().getMessageId().get())                                    // 2. FALSE: Get the referent msg
                        .flatMap(refMessage -> Mono.zip(refMessage.getAuthorAsMember().map(Member::getMention),
                            message.getAuthorAsMember().map(Member::getMention))
                            .flatMap(users -> generateResponse(refMessage.getContent(),
                                users.getT1(), users.getT2())
                                .flatMap(channel::createMessage)
                            )
                        )).then();
    }

    private String decorateMessageWithUserInfo(String content, String originalPoster, String answerer) {
        Optional<String> stringOptional = JavaFormatUtils.format(content).first();
        Optional<Throwable> throwableOptional = JavaFormatUtils.format(content).second();

        return stringOptional
            .map(s -> originalPoster + "'s code requested by " + answerer + ":" + String.format(JAVA_MESSAGE_TEMPLATE, s))
            .orElseGet(() -> "An error occured while requesting " + originalPoster + "'s code:```\n" + throwableOptional.get() + "\n```");
    }

    private Mono<String> generateResponse(String content, String originalPoster, String answerer) {
        String response = decorateMessageWithUserInfo(content, originalPoster, answerer);
        Mono<String> responseMono = Mono.just(response);

        if (response.length() > DISCORD_MAX_MESSAGE_LENGTH) {
            responseMono = Mono.fromFuture(Hastebin.paste("https://paste.md-5.net", response, false))
                .map(link -> originalPoster + "'s code requested by " + answerer + " was uploaded to " + link)
                .onErrorReturn("An error occured while uploading the formatted code. Try again later");
        }

        return responseMono;
    }
}
