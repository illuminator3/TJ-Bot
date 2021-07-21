package gg.discord.tj.bot.command.impl;

import discord4j.core.object.MessageReference;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import gg.discord.tj.bot.command.Command;
import gg.discord.tj.bot.command.CommandExecutionContext;

import java.util.*;
import java.util.stream.Collectors;

public class ProcessCommand
    implements Command
{
    @Override
    public String getName()
    {
        return "process";
    }

    @Override
    public Collection<String> getAliases()
    {
        return List.of("p");
    }

    @Override
    public void onExecute(CommandExecutionContext context)
    {
        Message message = context.getMessage();
        MessageChannel channel = Objects.requireNonNull(message.getChannel().block());
        Optional<MessageReference> referenceOpt = message.getMessageReference();

        if (referenceOpt.isEmpty() || !LinesCommand.LINE_MESSAGES.containsKey(referenceOpt.get().getMessageId().get()))
            channel.createMessage("This command works by replying to a ^lines message").block();
        else
        {
            Message replied = channel.getMessageById(referenceOpt.get().getMessageId().get()).block();
            List<String> lines = LinesCommand.LINE_MESSAGES.get(referenceOpt.get().getMessageId().get());
            String[] args = context.getContent().split(" ");

            if (args.length != 2)
            {
                channel.createMessage("Usage: ^process [start] [stop]").block();

                return;
            }

            int from, to;

            try
            {
                from = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignoredf)
            {
                channel.createMessage("Cannot read number: " + args[0]).block();

                return;
            }

            try
            {
                to = Integer.parseInt(args[1]);
            } catch (NumberFormatException ignoredf)
            {
                channel.createMessage("Cannot read number: " + args[1]).block();

                return;
            }

            List<String> sl = lines.subList(from - 1, to);
            var formatted = FormatCommand.format(String.join("\n", sl));
            Optional<String> product = formatted.getFirst();
            Optional<Throwable> lastThrowable = formatted.getSecond();
            String result;

            if (product.isEmpty())
                result = "An error occured while the formatted code:```\n" + lastThrowable.get().getMessage() + "\n```";
            else if (!product.get().isBlank())
                result = "The formatted code requested by " + message.getAuthorAsMember().block().getMention() + ":```java\n" + product.get() + "\n```";
            else
                result = "The formatted code requested by " + message.getAuthorAsMember().block().getMention() + ":```\n```";

            replied.edit(mes ->
                mes.setContent(result)).block();
        }
    }
}