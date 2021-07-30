package gg.discord.tj.bot.repository;

import gg.discord.tj.bot.command.Command;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public enum CommandRepository {
    INSTANCE;

    private final Map<String, Command> commands = new ConcurrentHashMap<>();

    public final void registerCommand(Command command) {
        commands.put(command.getName(), command);
        command.getAliases().forEach(s -> commands.put(s, command));
    }

    public final Optional<Command> retrieveCommand(String commandName) {
        return Optional.ofNullable(commands.get(commandName));
    }

    public final List<Command> commands() {
        return commands.values().stream().distinct().toList();
    }
}
