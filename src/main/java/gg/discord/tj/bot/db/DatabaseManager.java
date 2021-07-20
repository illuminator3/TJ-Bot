package gg.discord.tj.bot.db;

import gg.discord.tj.bot.util.ConfigurationContext;
import lombok.SneakyThrows;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.concurrent.atomic.AtomicReference;

public enum DatabaseManager {
    INSTANCE;
    
    private final AtomicReference<Connection> connectionRef = new AtomicReference<>();

    @SneakyThrows
    public Connection establishConnection() {
        var connection = connectionRef.get();
        if (connection == null) {
            connection = DriverManager.getConnection("jdbc:sqlite:" + Path.of("tjdatabase.db").toFile().getCanonicalPath());
            if (!connectionRef.compareAndSet(null, connection)) {
                return connectionRef.get();
            }
        }
        return connectionRef.get();
    }

    @SneakyThrows
    public void disconnect() {
        connectionRef.get().close();
    }
}
