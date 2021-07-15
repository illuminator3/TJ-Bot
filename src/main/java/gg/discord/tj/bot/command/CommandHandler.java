package gg.discord.tj.bot.command;

import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
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
        Message message = e.getMessage();
        String content = message.getContent();
        String[] s = content.split(" ");

        if (s.length != 0)
        {
            var ref = new Object()
                      { String f = s[0]; };

            if (ref.f.startsWith("^"))
            {
                ref.f = ref.f.substring(1);

                Command cmd = commands.stream().filter(c -> c.getName().equals(ref.f) || c.getAliasses().contains(ref.f)).findAny().orElse(null);

                if (cmd != null)
                {
                    CommandExecutionContext context = buildContext(e, ref.f);

                    cmd.onExecute(context);
                }
            }
        }
    }

    private CommandExecutionContext buildContext(MessageCreateEvent event, String cmd)
    {
        return new CommandExecutionContext(event.getMessage(), event.getMessage().getContent().substring(("^" + cmd).length()).trim(), event.getGuild().block(), event.getMember().orElseThrow());
    }
}