package gg.discord.tj.bot.db;

import gg.discord.tj.bot.util.Tuple;

import java.sql.*;

public class Database
{
    public static final Database DATABASE = new Database();

    private Database() {}

    private Connection connection;

    public void establishConnection(String to)
        throws SQLException
    {
        connection = DriverManager.getConnection("jdbc:sqlite:" + to);
    }

    public void disconnect()
        throws SQLException
    {
        connection.close();

        connection = null;
    }

    public Tuple<Statement, ResultSet> query(String sql, Object... obj)
        throws SQLException
    {
        Statement statement = connection.createStatement();

        return new Tuple<>(statement, statement.executeQuery(String.format(sql, obj)));
    }

    public Tuple<Statement, ResultSet> safeQuery(String sql, Object... obj)
    {
        try
        {
            return query(sql, obj);
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();

            throw new RuntimeException(throwables);
        }
    }

    public void update(String sql, Object... obj)
        throws SQLException
    {
        try (Statement statement = connection.createStatement())
        {
            statement.executeUpdate(String.format(sql, obj));
        }
    }

    public void safeUpdate(String sql, Object... obj)
    {
        try
        {
            update(sql, obj);
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
    }
}