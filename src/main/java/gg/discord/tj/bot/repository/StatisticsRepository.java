package gg.discord.tj.bot.repository;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public enum StatisticsRepository
{
    INSTANCE;

    final DatabaseManager databaseManager = DatabaseManager.INSTANCE;

    public void init()
        throws SQLException
    {
        Connection connection = databaseManager.establishConnection();

        try (Statement statement = connection.createStatement())
        {
            statement.execute("""
                    CREATE TABLE IF NOT EXISTS messages (
                        user long,
                        timestamp long,
                        guild long
                    );
                    """);
        }
    }

    public List<List<Long>> topNHelpersForGuild(long guildId, int limit)
        throws SQLException
    {
        List<List<Long>> topHelpersList = new ArrayList<>();
        Connection connection = databaseManager.establishConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement("""
                        WITH TOPHELPERS(user, count) AS (
                            SELECT user, count(*) FROM messages WHERE guild = ?
                            GROUP BY user ORDER BY count(*) DESC LIMIT ?
                        ) SELECT ROW_NUMBER() OVER(ORDER BY count DESC) as '#', user, count from TOPHELPERS
                        """))
        {

            preparedStatement.setLong(1, guildId);
            preparedStatement.setInt(2, limit);

            ResultSet result = preparedStatement.executeQuery();

            while (result.next())
            {
                long serialId = result.getLong("#");
                long userId = result.getLong("user");
                long msgCount = result.getLong("count");

                topHelpersList.add(List.of(serialId, userId, msgCount));
            }
        }

        return topHelpersList;
    }

    public int save(long guildId, long userId)
        throws SQLException
    {
        int rowCount;
        Connection connection = databaseManager.establishConnection();

        try (PreparedStatement preparedStatement = connection.prepareStatement("INSERT INTO messages (user, timestamp, guild) VALUES (?, CURRENT_TIMESTAMP, ?)"))
        {
            preparedStatement.setLong(1, userId);
            preparedStatement.setLong(2, guildId);

            rowCount = preparedStatement.executeUpdate();
        }

        return rowCount;
    }

    public int purge(long olderThanInMillis)
        throws SQLException
    {
        int rowCount;
        Connection connection = databaseManager.establishConnection();

        try (PreparedStatement prepareStatement = connection.prepareStatement("DELETE FROM messages WHERE timestamp < ?"))
        {
            prepareStatement.setLong(1, olderThanInMillis);

            rowCount = prepareStatement.executeUpdate();
        }

        return rowCount;
    }
}
