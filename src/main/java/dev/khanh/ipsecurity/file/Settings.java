package dev.khanh.ipsecurity.file;

import com.google.common.base.Preconditions;
import dev.khanh.ipsecurity.IPSecurityPlugin;
import dev.khanh.ipsecurity.data.DataStorageType;
import dev.khanh.ipsecurity.util.PluginLogger;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.TimeZone;

/**
 * Represents the configuration settings for the IPSecurityPlugin.
 * The Setting class handles loading, updating, and managing the configuration
 * settings for the {@link IPSecurityPlugin}.
 *
 * @author KhanhHuynh1402
 */
public class Settings {
    @Getter
    private final IPSecurityPlugin plugin;
    @Getter
    private final FileConfiguration defaultConfig;
    @Getter
    private final FileConfiguration config;
    @Getter
    private boolean isDebug;
    @Getter
    private DataStorageType dataStorageType;
    @Getter
    private boolean isShutdownServerOnDisable;
    @Getter
    private TimeZone timeZone;
    @Getter
    private SimpleDateFormat dateFormat;
    @Getter
    private int protectInterval;
    @Getter
    private boolean isRealtimeProtectEnable;
    @Getter
    private boolean isCheckOp;
    @Getter
    private boolean isCheckGamemode;
    @Getter
    private List<String> checkPermissions;
    @Getter
    private boolean isAddIPButtonEnable;
    @Getter
    private String addIPButtonText;
    @Getter
    private boolean isSendValidMessage;

    /**
     * Constructs a new Settings object.
     *
     * @param plugin The IPSecurityPlugin instance.
     */
    public Settings(IPSecurityPlugin plugin) {
        this.plugin = plugin;

        plugin.saveDefaultConfig();

        defaultConfig = loadDefaultConfig();
        config = plugin.getConfig();

        updateConfig();

        loadValues();
    }


    /**
     * Get the default config from the plugin's jar file
     * @return FileConfiguration default configuration
     */
    private FileConfiguration loadDefaultConfig() {
        InputStream inputStream = plugin.getResource("config.yml");
        assert inputStream != null;
        Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
        return YamlConfiguration.loadConfiguration(reader);
    }



    /**
     * Update the configuration if needed
     */
    private void updateConfig() {
        int defVersion = defaultConfig.getInt("config-version", 0);
        int currentVersion = config.getInt("config-version", 0);

        if (defVersion > currentVersion) {
            PluginLogger.info("Detected old version config trying to update");

            if (currentVersion == 0) {
                config.set("general.add-ip-button.enable", true);
                config.set("general.add-ip-button.text", "Add IP");
                config.set("general.send-valid-message", true);
            }

            config.set("config-version", defVersion);

            plugin.saveConfig();
        }
    }

    /**
     * Load config values
     */
    private void loadValues() {
        isDebug = config.getBoolean("debug");

        String type = config.getString("storage.storage-type");
        Preconditions.checkNotNull(type, "[config.yml] storage.storage-type is null");

        try {
            dataStorageType = DataStorageType.valueOf(type.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException(String.format("[config.yml] %s is invalid storage type", type));
        }

        isShutdownServerOnDisable = config.getBoolean("general.shutdown-on-disable", false);

        String timezoneID = config.getString("general.timezone");
        timeZone = TimeZone.getTimeZone(timezoneID);

        String stringDateFormat = config.getString("general.date-format");
        if (stringDateFormat == null) {
            PluginLogger.severe("[config.yml] general.date-format is null. Use default date format dd/MM/yyyy HH:mm:ss instead");
            dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        } else {
            try {
                dateFormat = new SimpleDateFormat(stringDateFormat);
            } catch (IllegalArgumentException e) {
                PluginLogger.severe(String.format("[config.yml] Invalid date format: %s. Use default date format dd/MM/yyyy HH:mm:ss instead", stringDateFormat));
                dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            }
        }

        isRealtimeProtectEnable = config.getBoolean("general.protect.enable");

        protectInterval = config.getInt("general.protect.interval");
        Preconditions.checkArgument(protectInterval > 0, "[config.yml] general.protect.interval must be a positive integer");

        isCheckOp = config.getBoolean("general.protect.methods.op");

        isCheckGamemode = config.getBoolean("general.protect.methods.gamemode");

        checkPermissions = config.getStringList("general.protect.methods.permissions");

        isAddIPButtonEnable = config.getBoolean("general.add-ip-button.enable");

        addIPButtonText = config.getString("general.add-ip-button.text", "");

        isSendValidMessage = config.getBoolean("general.send-valid-message");

    }

}
