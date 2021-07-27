package gg.discord.tj.bot.core;

import java.util.Map;

public interface Bot {
    void start();
    void reset();
    Map<String, String> getAvailableTags();
}
