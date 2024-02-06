package dev.khanh.ipsecurity.data;

import dev.khanh.ipsecurity.IPSecurityPlugin;
import dev.khanh.ipsecurity.util.PluginLogger;
import lombok.Getter;

import java.io.File;
import java.sql.*;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of {@link DataStorage} interface for SQLite database.
 *
 * @author KhanhHuynh1402
 */
public class SQLiteStorage implements DataStorage {
    @Getter
    private final Connection connection;

    /**
     * Constructor for SQLiteStorage.
     *
     * @param plugin The {@link IPSecurityPlugin} instance
     */
    public SQLiteStorage(IPSecurityPlugin plugin) {
        File file = new File(plugin.getDataFolder(), "database.db");

        String url = "jdbc:sqlite:" + file.getPath();

        try {

            PluginLogger.info("Initialize connection to SQLite.");
            connection = DriverManager.getConnection(url);
            PluginLogger.info("Successfully connected to SQLite");

            createTable();

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
    @Override
    public CompletableFuture<Void> setPlayerIP(String playerName, String ip) {
        return CompletableFuture.runAsync(() -> {
            String sql = "INSERT OR REPLACE INTO IPSecurity (PLAYER_NAME, IP) VALUES (?, ?)";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                preparedStatement.setString(1, playerName);
                preparedStatement.setString(2, ip);

                preparedStatement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<String> getPlayerIP(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "SELECT * FROM IPSecurity WHERE PLAYER_NAME = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

                preparedStatement.setString(1, playerName);

                ResultSet resultSet = preparedStatement.executeQuery();

                return resultSet.next() ? resultSet.getString("IP") : null;

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<Boolean> removePlayerIP(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            String sql = "DELETE FROM IPSecurity WHERE PLAYER_NAME = ?";

            try (PreparedStatement preparedStatement = connection.prepareStatement(sql)){

                preparedStatement.setString(1, playerName);

                return preparedStatement.executeUpdate() > 0;

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public void shutdown() {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }

    private void createTable() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS IPSecurity (PLAYER_NAME VARCHAR(64) PRIMARY KEY, IP VARCHAR(64) NOT NULL)");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
