package gg.discord.tj.bot.command.impl;

import discord4j.core.object.MessageReference;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import gg.discord.tj.bot.command.Command;
import gg.discord.tj.bot.command.CommandExecutionContext;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class SyntaxCommand
    implements Command {
    @Override
    public String getName() {
        return "syntax";
    }

    @Override
    public Collection<String> getAliases() {
        return List.of("s");
    }

    @Override
    public void onExecute(CommandExecutionContext context) {
        Message message = context.getMessage();
        MessageChannel channel = Objects.requireNonNull(message.getChannel().block());
        Optional<MessageReference> referenceOpt = message.getMessageReference();

        if (referenceOpt.isEmpty())
            channel.createMessage("This command works by replying to a message containing code").block();
        else {
            Message replied = channel.getMessageById(referenceOpt.get().getMessageId().get()).block();
            String content = replied.getContent();
            Optional<Throwable> thr = FormatCommand.format(content).getSecond();

            thr.ifPresentOrElse(
                t -> channel.createMessage(replied.getAuthorAsMember().block().getMention() + "'s code requested by " + message.getAuthorAsMember().block().getMention() + " will not compile:```\n" + t.getMessage() + "\n```").block(),
                () -> channel.createMessage(replied.getAuthorAsMember().block().getMention() + "'s code requested by " + message.getAuthorAsMember().block().getMention() + " has valid Syntax").block()
            );
        }
    }
}