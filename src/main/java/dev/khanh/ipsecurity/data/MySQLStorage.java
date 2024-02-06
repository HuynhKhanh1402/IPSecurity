package dev.khanh.ipsecurity.data;

import com.google.common.base.Preconditions;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import dev.khanh.ipsecurity.IPSecurityPlugin;
import dev.khanh.ipsecurity.util.PluginLogger;
import lombok.Getter;
import org.bukkit.configuration.ConfigurationSection;

import java.sql.*;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of {@link DataStorage} interface for MySQL database.
 *
 * @author KhanhHuynh1402
 */
public class MySQLStorage implements DataStorage {
    @Getter
    private final HikariDataSource dataSource;
    @Getter
    private final String host;
    @Getter
    private final int port;
    @Getter
    private final String database;
    @Getter
    private final String username;
    @Getter
    private final String password;
    @Getter
    private final String table;
    @Getter
    private final String parameters;
    @Getter
    private final int maximumPoolSize;
    @Getter
    private final int minimumIdle;
    @Getter
    private final int connectionTimeout;


    /**
     * Constructor for MySQL Storage.
     *
     * @param plugin The {@link IPSecurityPlugin} instance
     */
    public MySQLStorage(IPSecurityPlugin plugin) {
        ConfigurationSection section = plugin.getSettings().getConfig()
                .getConfigurationSection("storage.mysql-properties");
        Preconditions.checkNotNull(section, "[config.yml] storage.mysql-properties is null");

        host = section.getString("host");
        port = section.getInt("port");
        database = section.getString("database");
        username = section.getString("username");
        password = section.getString("password");
        table = section.getString("table");
        parameters = section.getString("parameters");
        maximumPoolSize = section.getInt("pool.maximum-pool-size");
        minimumIdle = section.getInt("pool.minimum-idle");
        connectionTimeout = section.getInt("pool.connection-timeout");

        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl(String.format("jdbc:mysql://%s:%d/%s%s", host, port, database, parameters));
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        PluginLogger.info("Connecting to MYSQL server...");
        dataSource = new HikariDataSource(hikariConfig);
        PluginLogger.info("Successfully connected to MYSQL");

        createTable();
    }
    @Override
    public CompletableFuture<Void> setPlayerIP(String playerName, String ip) {
        return CompletableFuture.runAsync(() -> {
            try (Connection connection = dataSource.getConnection()){

                String sql = String.format("INSERT INTO %s (PLAYER_NAME, IP) VALUES (?, ?) ON DUPLICATE KEY UPDATE IP = ?", table);
                PreparedStatement preparedStatement = connection.prepareStatement(sql);

                preparedStatement.setString(1, playerName);
                preparedStatement.setString(2, ip);
                preparedStatement.setString(3, ip);

                preparedStatement.executeUpdate();

            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        });
    }

    @Override
    public CompletableFuture<String> getPlayerIP(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try (Connection connection = dataSource.getConnection()){

                String sql = String.format("SELECT * FROM %s WHERE PLAYER_NAME = ?", table);
                PreparedStatement preparedStatement = connection.prepareStatement(sql);

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
           try (Connection connection = dataSource.getConnection()){

               String sql = String.format("DELETE FROM %s WHERE PLAYER_NAME = ?", table);
               PreparedStatement preparedStatement = connection.prepareStatement(sql);

               preparedStatement.setString(1, playerName);

               return preparedStatement.executeUpdate() > 0;

           } catch (SQLException e) {
               throw new RuntimeException(e);
           }
        });
    }

    @Override
    public void shutdown() {
        if (dataSource != null) {
            try {
                dataSource.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void createTable() {
        try (Connection connection = dataSource.getConnection()) {
            Statement statement = connection.createStatement();
            statement.executeUpdate(String.format(
                    "CREATE TABLE IF NOT EXISTS %s (PLAYER_NAME VARCHAR(64) PRIMARY KEY, IP VARCHAR(64) NOT NULL)",
                    table
            ));
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
