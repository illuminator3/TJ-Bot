package gg.discord.tj.bot.command.impl;

import com.github.freva.asciitable.HorizontalAlign;
import discord4j.core.object.entity.Message;
import gg.discord.tj.bot.command.Command;
import gg.discord.tj.bot.command.CommandExecutionContext;
import gg.discord.tj.bot.repository.CommandRepository;
import gg.discord.tj.bot.service.MessageService;
import gg.discord.tj.bot.util.PresentationUtils;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;

import static gg.discord.tj.bot.util.MessageTemplate.PLAINTEXT_MESSAGE_TEMPLATE;

public class HelpCommand implements Command {
    private final static CommandRepository COMMAND_REPOSITORY = CommandRepository.INSTANCE;
    public static final MessageService MESSAGE_SERVICE = new MessageService();

    @Override
    public String getName() {
        return "help";
    }

    @Override
    public Collection<String> getAliases() {
        return List.of("h");
    }

    @Override
    public String getDescription() {
        return "Provides helpful information about commands.";
    }

    @Override
    public Mono<Void> onExecute(CommandExecutionContext context) {
        Message message = context.message();
        
        return message
            .getChannel()
            .flatMap(channel -> channel == null ?
                Mono.empty() :
                channel.createMessage(String.format(PLAINTEXT_MESSAGE_TEMPLATE, PresentationUtils.dataFrameToAsciiTable(
                    fetchCommandInfoDataFrame(),
                    new String[] {"Command", "Alias", "Description"},
                    new HorizontalAlign[] {HorizontalAlign.LEFT, HorizontalAlign.LEFT, HorizontalAlign.LEFT}
                )))
                ).then();
    }

    private List<List<String>> fetchCommandInfoDataFrame() {
        return COMMAND_REPOSITORY.commands().stream()
            .map(command -> List.of(command.getName(), command.getAliases().toString().replaceAll("^.|.$", ""), command.getDescription()))
            .toList();
    }
}
