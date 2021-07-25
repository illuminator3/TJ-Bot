package gg.discord.tj.bot.command.impl;

import discord4j.core.object.MessageReference;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import gg.discord.tj.bot.command.Command;
import gg.discord.tj.bot.command.CommandExecutionContext;
import gg.discord.tj.bot.util.Hastebin;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class UploadCommand
    implements Command {
    @Override
    public String getName() {
        return "upload";
    }

    @Override
    public Collection<String> getAliases() {
        return List.of("u");
    }

    @Override
    public void onExecute(CommandExecutionContext context) {
        Message message = context.getMessage();
        MessageChannel channel = Objects.requireNonNull(message.getChannel().block());
        Optional<MessageReference> referenceOpt = message.getMessageReference();

        if (referenceOpt.isEmpty())
            channel.createMessage("This command works by replying to a message").block();
        else {
            Message replied = channel.getMessageById(referenceOpt.get().getMessageId().get()).block();

            Hastebin.paste("https://paste.md-5.net", replied.getContent(), false).whenComplete((link, thr) -> {
                if (thr != null)
                    channel.createMessage("An error occured while uploading. Try again later").block();
                else
                    channel.createMessage(replied.getAuthorAsMember().block().getMention() + "'s message requested by " + message.getAuthorAsMember().block().getMention() + " was uploaded to " + link).block();
            });
        }
    }
}