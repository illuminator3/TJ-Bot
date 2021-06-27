package gg.discord.tj.bot.command;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

@Getter
public class CommandHandler
{
    private final Set<Command> commands = new HashSet<>();

    public void registerCommand(Command command)
    {
        commands.add(command);
    }

    public void init(GatewayDiscordClient client)
    {
        client.on(MessageCreateEvent.class).subscribe(this::handleMessage);
    }

    private void handleMessage(MessageCreateEvent e)
    {
        String content = e.getMessage().getContent();

        if (content.startsWith("^"))
        {
            char commandCharacter = content.substring(1).charAt(0);
            Command command = commands.stream().filter(c -> c.getCommandCharacter() == commandCharacter).findAny().orElse(null);

            if (command != null)
                command.onExecute(buildContext(e));
        }
    }

    private CommandExecutionContext buildContext(MessageCreateEvent event)
    {
        return new CommandExecutionContext(event.getMessage(), event.getGuild().block(), event.getMember().orElseThrow());
    }
}