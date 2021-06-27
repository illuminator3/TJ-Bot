package gg.discord.tj.bot.util;

import lombok.Data;

@Data
public class Tuple<A, B>
{
    private final A first;
    private final B second;
}