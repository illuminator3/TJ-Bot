package gg.discord.tj.bot.util;

import java.util.Map;

public interface CountableMap<K>
    extends Map<K, Long>
{
    Long count(K by);
    long getCount(K by);
}