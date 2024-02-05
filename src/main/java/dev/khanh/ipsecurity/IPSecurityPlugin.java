package dev.khanh.ipsecurity;

import dev.khanh.ipsecurity.bot.DiscordBot;
import dev.khanh.ipsecurity.command.IPSecurityCommand;
import dev.khanh.ipsecurity.data.DataStorage;
import dev.khanh.ipsecurity.data.MySQLStorage;
import dev.khanh.ipsecurity.data.SQLiteStorage;
import dev.khanh.ipsecurity.data.YamlStorage;
import dev.khanh.ipsecurity.file.Messages;
import dev.khanh.ipsecurity.file.Settings;
import dev.khanh.ipsecurity.listener.PlayerListener;
import dev.khanh.ipsecurity.task.PlayerSecurityChecker;
import dev.khanh.ipsecurity.util.PluginLogger;
import dev.khanh.ipsecurity.util.VersionUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The main class of the IPSecurity plugin.
 *
 * @author KhanhHuynh1402
 */
public final class IPSecurityPlugin extends JavaPlugin {
    /**
     * Instance of the plugin.
     */
    @Getter
    private static IPSecurityPlugin instance;

    /**
     * Settings of the plugin.
     */
    @Getter
    private Settings settings;

    /**
     * Messages configuration of the plugin.
     */
    @Getter
    private Messages messages;

    /**
     * Data storage of the plugin.
     */
    @Getter
    private DataStorage dataStorage;

    /**
     * Discord bot instance
     */
    @Getter
    private DiscordBot discordBot;

    /**
     * Player security checker instance for monitoring player connections.
     */
    @Getter
    private PlayerSecurityChecker checker;

    @Override
    public void onEnable() {
        if (!VersionUtil.isCurrentServerVersionNewerOrEqual("1.16") && !isFullDependencyVersion()) {
            getLogger().severe(
                    "Your server version is older than 1.16, so it does not support the automatic " +
                    "library loading feature. Please download the jar-with-dependencies version from the GitHub " +
                    "releases page to work."
            );
            throw new UnsupportedOperationException();
        }

        instance = this;

        settings = new Settings(this);

        messages = new Messages(this);

        setupDataStorage();

        discordBot = new DiscordBot(this);

        if (settings.isRealtimeProtectEnable()) {
            checker = new PlayerSecurityChecker(this);
        }

        printCheckingMethodInfo();

        registerListeners(new PlayerListener(this));

        registerCommand();
    }

    @Override
    public void onDisable() {
        if (checker != null) {
            checker.cancel();
        }

        if (dataStorage != null) {
            dataStorage.shutdown();
        }

        if (discordBot != null) {
            discordBot.shutdown();
        }

        shutdownServer();
    }

    /**
     * Sets up the data storage based on the configuration settings.
     */
    private void setupDataStorage() {
        switch (settings.getDataStorageType()) {
            case MYSQL: {
                dataStorage = new MySQLStorage(this);
                return;
            }
            case SQLITE: {
                dataStorage = new SQLiteStorage(this);
                return;
            }
            case YAML: {
                dataStorage = new YamlStorage(this);
                return;
            }
            default: {
                throw new RuntimeException("Invalid data storage type");
            }
        }
    }

    /**
     * Prints information about the enabled protection methods.
     */
    private void printCheckingMethodInfo() {
        if (!settings.isRealtimeProtectEnable()) {
            PluginLogger.warning("Realtime protect is " + ChatColor.RED + "DISABLED");
        } else {
            PluginLogger.info("Protecting methods:");
            PluginLogger.info(String.format(" %s  OP", (settings.isCheckOp() ? "✔" : "✘")));
            PluginLogger.info(String.format(" %s  Gamemode", (settings.isCheckGamemode() ? "✔" : "✘")));
            PluginLogger.info(String.format(" %s  Permission", (settings.getCheckPermissions().size() != 0 ? "✔" : "✘")));
        }
    }

    /**
     * Registers the provided event listeners.
     *
     * @param listeners The event {@link Listener} to register.
     */
    private void registerListeners(Listener... listeners) {
        for (Listener listener : listeners) {
            getServer().getPluginManager().registerEvents(listener, this);
        }
    }

    /**
     * Registers the IPSecurity command and its executor.
     */
    private void registerCommand() {
        IPSecurityCommand command = new IPSecurityCommand(this);
        PluginCommand pluginCommand = getCommand("ipsecurity");
        assert pluginCommand != null;
        pluginCommand.setExecutor(command);
        pluginCommand.setTabCompleter(command);
    }

    /**
     * Shuts down the server if configured to do so on plugin disable.
     */
    private void shutdownServer() {
        if (settings != null && settings.isShutdownServerOnDisable()) {
            getLogger().warning("Shutting down server...");
            Bukkit.getServer().shutdown();
        }
    }

    /**
     * Reloads the messages configuration.
     */
    public void reloadMessages() {
        messages = new Messages(this);
    }


    /**
     * Check if this plugin is jar-with-dependencies version.
     * @return true if this plugin is jar-with-dependencies version, otherwise false
     */
    private boolean isFullDependencyVersion() {
        try {
            Class.forName("net.dv8tion.jda.api.JDA");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

}
