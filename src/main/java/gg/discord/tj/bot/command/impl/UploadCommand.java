package gg.discord.tj.bot.command.impl;

import discord4j.core.object.MessageReference;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import gg.discord.tj.bot.command.Command;
import gg.discord.tj.bot.command.CommandExecutionContext;
import gg.discord.tj.bot.util.Hastebin;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class UploadCommand implements Command {
    @Override
    public String getName() {
        return "upload";
    }

    @Override
    public Collection<String> getAliases() {
        return List.of("u");
    }

    @Override
    public String getDescription() {
        return "Uploads to Hastebin when issued against a message.";
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
                    channel.createMessage("This command works by replying to a message containing code") : // 2. TRUE: Reply back that it wont work.
                    channel.getMessageById(referenceOpt.get().getMessageId().get())                        // 2. FALSE: Get the referent msg
                        .flatMap(refMessage -> Mono.zip(refMessage.getAuthorAsMember().map(Member::getMention),
                            message.getAuthorAsMember().map(Member::getMention))
                            .flatMap(users -> decorateMessageWithUserInfo(refMessage.getContent(), users.getT1(), users.getT2())
                                .flatMap(channel::createMessage))
                        )).then();
    }

    private Mono<String> decorateMessageWithUserInfo(String content, String originalPoster, String answerer) {
        return Mono.fromFuture(Hastebin.paste("https://paste.md-5.net", content, false))
            .map(link -> originalPoster + "'s code requested by " + answerer + " was uploaded to " + link)
            .onErrorReturn("An error occured while uploading the formatted code. Try again later");
    }
}
