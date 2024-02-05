package dev.khanh.ipsecurity.listener;

import dev.khanh.ipsecurity.IPSecurityPlugin;
import dev.khanh.ipsecurity.file.Messages;
import dev.khanh.ipsecurity.task.PlayerSecurityChecker;
import dev.khanh.ipsecurity.util.TaskUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 * A listener class to handle events related to player events.
 *
 * @author KhanhHuynh1402
 */
public class PlayerListener implements Listener {
    @Getter
    private final IPSecurityPlugin plugin;

    /**
     * Constructs a new PlayerListener.
     *
     * @param plugin The IPSecurityPlugin instance.
     */
    public PlayerListener(IPSecurityPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the PlayerJoinEvent.
     *
     * @param event The PlayerJoinEvent instance.
     */
    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
           Player player = event.getPlayer();

           if (!player.isOnline()) {
               return;
           }

            PlayerSecurityChecker checker = plugin.getChecker();

            TaskUtil.runAsync(() -> {
                if (!checker.isValidPlayer(player)) {
                    checker.handleInvalidPlayer(player);
                } else {
                    handleValidPlayer(player);
                }
            });
        });
    }

    /**
     * Handles a valid player.
     *
     * @param player The {@link Player} instance.
     */
    private void handleValidPlayer(Player player) {
        Messages messages = plugin.getMessages();

        player.sendMessage(ChatColor.translateAlternateColorCodes('&', messages.getVerifiedMessage()));
        TaskUtil.runAsync(() -> plugin.getDiscordBot().sendNotification(messages.getVerifiedMessageEmbed(player)));
    }
}
