package gg.discord.tj.bot.command.impl;

import discord4j.common.util.Snowflake;
import discord4j.core.object.MessageReference;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import gg.discord.tj.bot.command.Command;
import gg.discord.tj.bot.command.CommandExecutionContext;

import java.util.*;
import java.util.stream.Collectors;

public class LinesCommand
    implements Command
{
    protected static final Map<Snowflake, List<String>> LINE_MESSAGES = new WeakHashMap<>();

    @Override
    public String getName()
    {
        return "lines";
    }

    @Override
    public Collection<String> getAliases()
    {
        return List.of("l");
    }

    @Override
    public void onExecute(CommandExecutionContext context)
    {
        Message message = context.getMessage();
        MessageChannel channel = Objects.requireNonNull(message.getChannel().block());
        Optional<MessageReference> referenceOpt = message.getMessageReference();

        if (referenceOpt.isEmpty())
            channel.createMessage("This command works by replying to a message").block();
        else
        {
            Message replied = channel.getMessageById(referenceOpt.get().getMessageId().get()).block();
            String content = replied.getContent();
            String lines = content.replace("`", "\\`");
            List<String> st = Arrays.asList(lines.split("\n"));
            int length = String.valueOf(st.size()).length();
            var ref = new Object()
                     { int c = 1; };

            lines = st.stream().map(s -> " ".repeat(length).substring(String.valueOf(ref.c).length()) + ref.c++ + ": " + s).collect(Collectors.joining("\n"));

            Message msg = channel.createMessage("Line numbers as from " + replied.getAuthorAsMember().block().getMention() + "'s message requested by " + message.getAuthorAsMember().block().getMention() + ":```\n" + lines + "\n```").block();

            LINE_MESSAGES.put(msg.getId(), st);
        }
    }
}