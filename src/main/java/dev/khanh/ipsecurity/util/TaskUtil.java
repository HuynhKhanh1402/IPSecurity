package dev.khanh.ipsecurity.util;

import dev.khanh.ipsecurity.IPSecurityPlugin;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

/**
 * Utility class for running tasks asynchronously and synchronously.
 *
 * @author KhanhHuynh1402
 */
public class TaskUtil {
    /**
     * Runs a task asynchronously.
     *
     * @param task The task to run.
     */
    public static void runAsync(@NotNull Runnable task) {
        Bukkit.getScheduler().runTaskAsynchronously(IPSecurityPlugin.getInstance(), task);
    }

    /**
     * Runs a task synchronously.
     *
     * @param task The task to run.
     */
    public static void runSync(@NotNull Runnable task) {
        Bukkit.getScheduler().runTask(IPSecurityPlugin.getInstance(), task);
    }
}
