package gg.discord.tj.bot.util;

import java.util.TreeMap;

public class CountableMap<K>
    extends TreeMap<K, Long>
{
    public Long count(K by)
    {
        return !containsKey(by) ? put(by, 1L) : compute(by, (k, v) -> v + 1);
    }

    public long getCount(K by)
    {
        return get(by);
    }
}