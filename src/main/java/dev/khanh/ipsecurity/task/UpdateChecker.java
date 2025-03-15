package dev.khanh.ipsecurity.task;

import dev.khanh.ipsecurity.IPSecurityPlugin;
import dev.khanh.ipsecurity.util.PluginLogger;
import dev.khanh.ipsecurity.util.VersionUtil;
import lombok.Getter;
import org.bukkit.ChatColor;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * A task check for plugin updates.
 */
@Getter
public class UpdateChecker implements Runnable {
    private final IPSecurityPlugin plugin;
    private final int resourceID;

    /**
     * Constructs an UpdateChecker object.
     *
     * @param plugin     The {@link IPSecurityPlugin} instance.
     * @param resourceID The resource ID of the plugin on SpigotMC.
     */
    public UpdateChecker(IPSecurityPlugin plugin, int resourceID) {
        this.plugin = plugin;
        this.resourceID = resourceID;

        plugin.getScheduler().runTaskTimerAsynchronously(this, 0, TimeUnit.HOURS.toSeconds(3) * 20);
    }

    /**
     * Checks for updates.
     */
    @Override
    public void run() {
        try (InputStream inputStream = new URL("https://api.spigotmc.org/legacy/update.php?resource=" + this.resourceID + "/~").openStream();
             Scanner scanner = new Scanner(inputStream)) {

            if (scanner.hasNext()) {

                String latestVersion = scanner.next();
                String currentVersion = plugin.getDescription().getVersion();

                int compareResult = VersionUtil.compareVersions(latestVersion, currentVersion);

                if (compareResult <= 0) {
                    PluginLogger.info("You are on the latest version.");
                } else {

                    PluginLogger.warning("There is a new version available!");
                    PluginLogger.warning("Your version:   " + ChatColor.RED + currentVersion);
                    PluginLogger.warning("Latest version: " + ChatColor.GREEN + latestVersion);
                    PluginLogger.warning("Please update to newest version.");
                    PluginLogger.warning("Link: https://www.spigotmc.org/resources/ipsecurity-1-8-1-20.114940/");

                }
            }

        } catch (IOException e) {
            plugin.getLogger().info("Unable to check for updates: " + e.getMessage());
        }
    }
}
