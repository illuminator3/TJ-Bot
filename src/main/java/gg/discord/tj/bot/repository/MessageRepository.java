package gg.discord.tj.bot.repository;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import discord4j.core.object.entity.Message;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static gg.discord.tj.bot.util.Constants.PURGABLE_COMMAND_EXPIRY_TIME_IN_MINUTES;
import static gg.discord.tj.bot.util.Constants.PURGABLE_COMMAND_MAX_ENTRIES;

public enum MessageRepository {
    INSTANCE;

    final DatabaseManager databaseManager = DatabaseManager.INSTANCE;
    final Cache<Long, Message> purgableCommandResponseReferences = CacheBuilder.newBuilder()
        .maximumSize(PURGABLE_COMMAND_MAX_ENTRIES)
        .expireAfterWrite(PURGABLE_COMMAND_EXPIRY_TIME_IN_MINUTES, TimeUnit.MINUTES)
        .build();

    public void init() throws SQLException {
        Connection connection = databaseManager.establishConnection();

        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS messages (
                        user long,
                        timestamp long,
                        guild long
                    );
                    """);
        }
    }

    public List<List<Long>> topNHelpersForGuild(long guildId, int limit) throws SQLException {
        List<List<Long>> topHelpersList = new ArrayList<>();
        Connection connection = databaseManager.establishConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement("""
                        WITH TOPHELPERS(user, count) AS (
                            SELECT user, count(*) FROM messages WHERE guild = ?
                            GROUP BY user ORDER BY count(*) DESC LIMIT ?
                        ) SELECT ROW_NUMBER() OVER(ORDER BY count DESC) as '#', user, count from TOPHELPERS
                        """)) {
            preparedStatement.setLong(1, guildId);
            preparedStatement.setInt(2, limit);

            ResultSet result = preparedStatement.executeQuery();

            while (result.next()) {
                long serialId = result.getLong("#");
                long userId = result.getLong("user");
                long msgCount = result.getLong("count");

                topHelpersList.add(List.of(serialId, userId, msgCount));
            }
        }

        return topHelpersList;
    }

    public int save(long guildId, long userId) throws SQLException {
        int rowCount;
        Connection connection = databaseManager.establishConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO messages (user, timestamp, guild) VALUES (?, CURRENT_TIMESTAMP, ?)")) {
            preparedStatement.setLong(1, userId);
            preparedStatement.setLong(2, guildId);

            rowCount = preparedStatement.executeUpdate();
        }

        return rowCount;
    }

    public int purge(long olderThanInMillis) throws SQLException {
        int rowCount;
        Connection connection = databaseManager.establishConnection();

        try (PreparedStatement prepareStatement = connection.prepareStatement("DELETE FROM messages WHERE timestamp < ?")) {
            prepareStatement.setLong(1, olderThanInMillis);

            rowCount = prepareStatement.executeUpdate();
        }

        return rowCount;
    }

    public void setPurgableCommandResponseReference(long msgId, Message message) {
        purgableCommandResponseReferences.put(msgId, message);
    }

    public Message getPurgableCommandResponseReference(long msgId) {
        return purgableCommandResponseReferences.getIfPresent(msgId);
    }
}
