package gg.discord.tj.bot.command.impl;

import discord4j.core.object.MessageReference;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import gg.discord.tj.bot.command.Command;
import gg.discord.tj.bot.command.CommandExecutionContext;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LinesCommand implements Command {
    @Override
    public String getName() {
        return "lines";
    }

    @Override
    public Collection<String> getAliases() {
        return List.of("l");
    }

    @Override
    public String getDescription() {
        return "Prints line numbers when issued against a message.";
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
                            .flatMap(users -> channel.createMessage(messageCreateSpec -> messageCreateSpec
                                .setContent(decorateMessageWithUserInfo(addLineNumbers(refMessage.getContent()), users.getT1(), users.getT2()))
                                .setMessageReference(refMessage.getId())
                            ))
                        )).then();

    }

    private String addLineNumbers(String content) {
        String lines = content.replace("`", "\\`");
        List<String> st = Arrays.asList(lines.split("\n"));
        int length = String.valueOf(st.size()).length();
        var ref = new Object() {
            int c = 1;
        };

        return st.stream()
            .map(s -> " ".repeat(length).substring(String.valueOf(ref.c).length()) + ref.c++ + ": " + s)
            .collect(Collectors.joining("\n"));
    }

    private String decorateMessageWithUserInfo(String content, String originalPoster, String answerer) {
        return "Line numbers as from " + originalPoster + "'s message requested by " + answerer + ":```\n" + content + "\n```";
    }
}
