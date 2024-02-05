package dev.khanh.ipsecurity.util;

import dev.khanh.ipsecurity.IPSecurityPlugin;
import dev.khanh.ipsecurity.file.Settings;

/**
 * Utility class for logging messages in the IPSecurityPlugin.
 * This class provides static methods to log information, warnings, and severe
 * messages using the plugin's logger.
 *
 * @author KhanhHuynh1402
 *
 */
public class PluginLogger {

    /**
     * Logs an informational message.
     *
     * @param message The message to be logged.
     */
    public static void info(String message) {
        IPSecurityPlugin.getInstance().getLogger().info(message);
    }

    /**
     * Logs a warning message.
     *
     * @param message The warning message to be logged.
     */
    public static void warning(String message) {
        IPSecurityPlugin.getInstance().getLogger().warning(message);
    }

    /**
     * Logs a severe (error) message.
     *
     * @param message The severe message to be logged.
     */
    public static void severe(String message) {
        IPSecurityPlugin.getInstance().getLogger().severe(message);
    }

    /**
     * Logs a debug message.
     *
     * @param message The debug message to be logged.
     */
    public static void debug(String message) {
        Settings settings = IPSecurityPlugin.getInstance().getSettings();
        if (settings == null) {
            return;
        }

        if (settings.isDebug()) {
            info("[DEBUG] " + message);
        }
    }
}