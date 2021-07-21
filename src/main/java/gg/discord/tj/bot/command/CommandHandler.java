package gg.discord.tj.bot.command;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;

@Getter
public class CommandHandler
{
    private final Map<String, Command> commands = new HashMap<>();

    public void registerCommand(Command command)
    {
        commands.put(command.getName(), command);

        command.getAliases().forEach(s -> commands.put(s, command));
    }

    public void init(GatewayDiscordClient client)
    {
        client.on(MessageCreateEvent.class).subscribe(this::handleMessage);
    }

    private void handleMessage(MessageCreateEvent e)
    {
        Message message = e.getMessage();
        String content = message.getContent();
        String[] s = content.split(" ");

        if (s.length != 0)
        {
            String commandRaw = s[0];

            if (commandRaw.startsWith("^"))
            {
                String commandName = commandRaw.substring(1);

                commands.computeIfPresent(commandName,
                    extraIdentity((name, command) -> command.onExecute(buildContext(e, name)))
                );
            }
        }
    }

    private CommandExecutionContext buildContext(MessageCreateEvent event, String cmd)
    {
        return new CommandExecutionContext(event.getMessage(), event.getMessage().getContent().substring(("^" + cmd).length()).trim(), event.getGuild().block(), event.getMember().orElseThrow());
    }

    public static <A, B> BiFunction<A, B, B> extraIdentity(BiConsumer<A, B> consumer)
    {
        return (a, b) -> {
            consumer.accept(a, b);

            return b;
        };
    }
}