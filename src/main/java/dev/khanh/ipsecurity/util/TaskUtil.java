package dev.khanh.ipsecurity.util;

import dev.khanh.ipsecurity.IPSecurityPlugin;
import org.bukkit.entity.Entity;
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
        IPSecurityPlugin.getInstance().getScheduler().runTaskAsynchronously(task);
    }

    /**
     * Runs a task synchronously.
     *
     * @param entity The entity of task
     * @param task The task to run.
     */
    public static void runSync(@NotNull Entity entity, @NotNull Runnable task) {
        IPSecurityPlugin.getInstance().getScheduler().runTask(entity, task);
    }
}
