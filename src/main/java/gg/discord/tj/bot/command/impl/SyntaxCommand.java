package gg.discord.tj.bot.command.impl;

import com.google.googlejavaformat.java.Formatter;
import com.google.googlejavaformat.java.FormatterException;
import discord4j.core.object.MessageReference;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import gg.discord.tj.bot.command.Command;
import gg.discord.tj.bot.command.CommandExecutionContext;

import java.util.*;
import java.util.function.Function;

public class SyntaxCommand
    implements Command
{
    private static final Formatter FORMATTER = new Formatter();

    @Override
    public String getName()
    {
        return "syntax";
    }

    @Override
    public Collection<String> getAliases()
    {
        return List.of("s");
    }

    @Override
    public void onExecute(CommandExecutionContext context)
    {
        Message message = context.getMessage();
        MessageChannel channel = Objects.requireNonNull(message.getChannel().block());
        Optional<MessageReference> referenceOpt = message.getMessageReference();

        if (referenceOpt.isEmpty())
            channel.createMessage("This command works by replying to a message containing code").block();
        else
        {
            Message replied = channel.getMessageById(referenceOpt.get().getMessageId().get()).block();
            String content = replied.getContent();
            boolean works = false;
            Optional<Throwable> result = Optional.empty();
            List<Map.Entry<Function<String, String>, Function<String, String>>> phases = Arrays.asList(
                    Map.entry(Function.identity(), Function.identity()),
                    Map.entry(s -> "public class A{" + s + "}", s -> s.substring("public class A {\n".length(), s.length() - "\n}".length()).replaceAll(" {2}(.+)", "$1")),
                    Map.entry(s -> "public class A{public<T>T b(){" + s + "}}", s -> s.substring("public class A {\n  public <T> T b() {".length(), s.length() - "\n  }\n}".length()).replaceAll(" {4}(.+)", "$1"))
            );

            for (var phase : phases)
            {
                try
                {
                    FORMATTER.formatSource(
                            phase.getKey().apply(
                                    content));

                    works = true;
                } catch (FormatterException ex)
                {
                    result = Optional.of(ex);
                }
            }

            if (result.isEmpty() || works)
                channel.createMessage(replied.getAuthorAsMember().block().getMention() + "'s code requested by " + message.getAuthorAsMember().block().getMention() + " has valid Syntax").block();
            else
                channel.createMessage(replied.getAuthorAsMember().block().getMention() + "'s code requested by " + message.getAuthorAsMember().block().getMention() + " will not compile:```\n" + result.get().getMessage() + "\n```").block();
        }
    }
}