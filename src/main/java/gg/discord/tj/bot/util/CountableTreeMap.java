package gg.discord.tj.bot.util;

import java.util.TreeMap;

public class CountableTreeMap<K>
    extends TreeMap<K, Long>
    implements CountableMap<K>
{
    public Long count(K by)
    {
        return merge(by, 1L, Long::sum);
    }

    public long getCount(K by)
    {
        return get(by);
    }
}