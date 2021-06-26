package gg.discord.tj.bot.util;

import java.util.Map;

public interface CountableMap<K>
    extends Map<K, Long>
{
    long count(K by);
    long getCount(K by);
}