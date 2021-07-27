package gg.discord.tj.bot.command;

import discord4j.core.object.entity.Message;

public record CommandExecutionContext(Message message, String commandContent) {}
