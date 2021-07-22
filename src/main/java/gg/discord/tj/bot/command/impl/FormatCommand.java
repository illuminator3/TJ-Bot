package gg.discord.tj.bot.command.impl;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import discord4j.core.object.MessageReference;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import gg.discord.tj.bot.command.Command;
import gg.discord.tj.bot.command.CommandExecutionContext;
import gg.discord.tj.bot.util.Hastebin;
import gg.discord.tj.bot.util.Tuple;

import java.util.*;
import java.util.function.Function;

public class FormatCommand
    implements Command
{
    private static final Formatter FORMATTER = new Formatter();

    @Override
    public String getName()
    {
        return "format";
    }

    @Override
    public Collection<String> getAliases()
    {
        return List.of("f");
    }

    @Override
    public void onExecute(CommandExecutionContext context)
    {
        Message message = context.getMessage();
        MessageChannel channel = Objects.requireNonNull(message.getChannel().block());
        Optional<MessageReference> referenceOpt = message.getMessageReference();

        if (referenceOpt.isEmpty())
            channel.createMessage("This command works by replying to a message containing unformatted code").block();
        else
        {
            Message replied = channel.getMessageById(referenceOpt.get().getMessageId().get()).block();
            String content = replied.getContent();
            Tuple<Optional<String>, Optional<Throwable>> formatted = format(content);
            Optional<String> product = formatted.getFirst();
            Optional<Throwable> lastThrowable = formatted.getSecond();

            if (product.isEmpty())
                channel.createMessage("An error occured while requesting " + replied.getAuthorAsMember().block().getMention() + "'s code:```\n" + lastThrowable.get().getMessage() + "\n```").block();
            else
            {
                String result = product.get(), msg = replied.getAuthorAsMember().block().getMention() + "'s code requested by " + message.getAuthorAsMember().block().getMention() + ":```java\n" + result + "\n```";

                if (msg.length() > 2000) // discord character limit
                {
                    Hastebin.paste("https://paste.md-5.net", result, false).whenComplete((link, thr) -> {
                        if (thr != null)
                            channel.createMessage("An error occured while uploading the formatted code. Try again later").block();
                        else
                            channel.createMessage(replied.getAuthorAsMember().block().getMention() + "'s code requested by " + message.getAuthorAsMember().block().getMention() + " was uploaded to " + link).block();
                    });
                }
                else channel.createMessage(msg).block();
            }
        }
    }

    public static Tuple<Optional<String>, Optional<Throwable>> format(String input)
    {
        if (input.isEmpty()) return new Tuple<>(Optional.of(""), Optional.empty());

        Optional<String> product = Optional.empty();
        Optional<Throwable> lastThrowable = Optional.empty();
        List<Map.Entry<Function<String, String>, Function<String, String>>> phases = Arrays.asList(
                Map.entry(Function.identity(), Function.identity()),
                Map.entry(s -> "public class A{" + s + "}", s -> s.substring("public class A {\n".length(), s.length() - "\n}".length()).replaceAll(" {2}(.+)", "$1")),
                Map.entry(s -> "public class A{public<T>T b(){" + s + "}}", s -> s.substring("public class A {\n  public <T> T b() {".length(), s.length() - "\n  }\n}".length()).replaceAll(" {4}(.+)", "$1"))
        );

        for (Map.Entry<Function<String, String>, Function<String, String>> phase : phases)
        {
            try
            {
                product = Optional.of(
                        phase.getValue().apply(
                                FORMATTER.formatSource(
                                        phase.getKey().apply(
                                                input))));
            } catch (FormatterException ex)
            {
                lastThrowable = Optional.of(ex);
            }
        }

        return new Tuple<>(product, lastThrowable);
    }
}