package gg.discord.tj.bot.command.impl;

import gg.discord.tj.bot.command.Command;
import gg.discord.tj.bot.command.CommandExecutionContext;
import gg.discord.tj.bot.repository.CommandRepository;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public class HelpCommand
    implements Command
{
    private final static CommandRepository COMMAND_REPOSITORY = CommandRepository.INSTANCE;

    @Override
    public String getName()
    {
        return "help";
    }

    @Override
    public Collection<String> getAliases()
    {
        return List.of("h");
    }

    @Override
    public Mono<Void> onExecute(CommandExecutionContext context)
    {
        return context.message()
            .getChannel()
            .flatMap(channel -> channel == null ?
                Mono.empty() :
                channel.createMessage("""
                        -- Commands
                         """ + COMMAND_REPOSITORY.commands().stream().map(c -> c.getClass().getSimpleName() + " (^" + c.getName() + ")").collect(Collectors.joining("\n"))
                )).then();
    }
}
