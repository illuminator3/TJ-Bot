package gg.discord.tj.bot.command;

import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.object.entity.Message;
import lombok.Data;

@Data
public class CommandExecutionContext {
    private final Message message;
    private final String content;
    private final Guild guild;
    private final Member member;
}