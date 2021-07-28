package gg.discord.tj.bot.command.impl;

import discord4j.core.object.MessageReference;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import gg.discord.tj.bot.command.Command;
import gg.discord.tj.bot.command.CommandExecutionContext;
import gg.discord.tj.bot.util.Hastebin;
import gg.discord.tj.bot.util.JavaFormatUtils;
import gg.discord.tj.bot.util.Tuple;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static gg.discord.tj.bot.util.Constants.DISCORD_MAX_MESSAGE_LENGTH;
import static gg.discord.tj.bot.util.MessageTemplate.JAVA_MESSAGE_TEMPLATE;

public class ProcessCommand implements Command {
    private final static Pattern COMMAND_ARGS_PATTERN = Pattern.compile("^(\\d+)(?:\s+(\\d+))?.*");

    private static Tuple<Optional<Tuple<Integer, Integer>>, Optional<String>> parseLineNumberArgsAndValidate(String args, int lineCount) {
        Optional<Tuple<Integer, Integer>> argsTuple = Optional.empty();
        Optional<String> msgTuple = Optional.empty();
        Matcher matcher = COMMAND_ARGS_PATTERN.matcher(args);

        if (matcher.matches()) {
            Integer from = Integer.parseInt(matcher.group(1)) < 1 ? 1 : Integer.parseInt(matcher.group(1));       // Lower bound to 1
            Integer to = matcher.group(2) == null ? lineCount :
                Integer.parseInt(matcher.group(2)) > lineCount ? lineCount : Integer.parseInt(matcher.group(2));  // Upper bound to lineCount

            if (from.compareTo(to) > 0) {
                msgTuple = Optional.of("range from: " + from + " cannot be greater than to: " + to);
            } else if (from.intValue() > lineCount) {
                msgTuple = Optional.of("range from: " + from + " cannot be greater than the total no. of lines: " + lineCount);
            } else {
                argsTuple = Optional.of(new Tuple<>(from, to));
            }
        } else {
            msgTuple = Optional.of("Usage: ^process start [stop]");
        }

        return new Tuple<>(argsTuple, msgTuple);
    }

    @Override
    public String getName() {
        return "process";
    }

    @Override
    public Collection<String> getAliases() {
        return List.of("p");
    }

    @Override
    public String getDescription() {
        return """
            Analogous to format command but used in conjunction with lines command.
            Formats/prettifies source code from the provided selection of line nos.
            eg. ^process START_LINE_NO [STOP_LINE_NO]
            """;
    }

    @Override
    public Mono<Void> onExecute(CommandExecutionContext context) {
        String args = context.commandContent();
        Message message = context.message();
        Mono<MessageChannel> channelMono = message.getChannel();
        Optional<MessageReference> referenceOpt = message.getMessageReference();
        Mono<Message> originalPosterMessage = referenceOpt.isEmpty() ?
            Mono.empty() :
            channelMono.flatMap(channel -> channel.getMessageById(referenceOpt.get().getMessageId().get()))
                .flatMap(botMessage -> botMessage.getMessageReference().isEmpty() ?
                    Mono.empty() :
                    channelMono.flatMap(channel -> channel.getMessageById(botMessage.getMessageReference().get().getMessageId().get())));

        return context.message()
            .getChannel()
            .flatMap(channel -> channel == null ? // 1. Check if channel is empty. May be it was deleted
                Mono.empty() :                    // 1. TRUE: Nothing to action
                referenceOpt.isEmpty() ?          // 1. FALSE: 2. Check if the current msg has a msg reference ie. if this was in reply to an earlier msg.
                    channel.createMessage("This command works by replying to a ^lines message") : // 2. TRUE: Reply back that it wont work.
                    originalPosterMessage.flatMap(opMsg -> Mono.zip(
                        opMsg.getAuthorAsMember().map(Member::getMention),
                        message.getAuthorAsMember().map(Member::getMention)
                        )
                            .flatMap(users -> generateResponse(
                                opMsg.getContent(), args,
                                users.getT1(), users.getT2()
                                ).flatMap(channel::createMessage)
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

    private Mono<String> generateResponse(String content, String args, String originalPoster, String answerer) {
        String response;
        String sanitizedContent = content.replace("`", "\\`");
        List<String> lines = Arrays.asList(sanitizedContent.split("\n"));
        Tuple<Optional<Tuple<Integer, Integer>>, Optional<String>> validatedArgsTuple = parseLineNumberArgsAndValidate(args, lines.size());

        if (validatedArgsTuple.first().isPresent()) {
            Tuple<Integer, Integer> argsTuple = validatedArgsTuple.first().get();
            List<String> codeBlockLines = lines.subList(argsTuple.first() - 1, argsTuple.second());
            String codeBlock = String.join("\n", codeBlockLines);
            response = decorateMessageWithUserInfo(codeBlock, originalPoster, answerer);
        } else {
            response = validatedArgsTuple.second().get();
        }

        Mono<String> responseMono = Mono.just(response);

        if (response.length() > DISCORD_MAX_MESSAGE_LENGTH) {
            responseMono = Mono.fromFuture(Hastebin.paste("https://paste.md-5.net", response, false))
                .map(link -> originalPoster + "'s code requested by " + answerer + " was uploaded to " + link)
                .onErrorReturn("An error occured while uploading the formatted code. Try again later");
        }

        return responseMono;
    }
}
