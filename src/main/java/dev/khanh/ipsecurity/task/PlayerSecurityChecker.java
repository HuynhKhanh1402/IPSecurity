package dev.khanh.ipsecurity.task;

import dev.khanh.ipsecurity.IPSecurityPlugin;
import dev.khanh.ipsecurity.bot.DiscordBot;
import dev.khanh.ipsecurity.file.Messages;
import dev.khanh.ipsecurity.file.Settings;
import dev.khanh.ipsecurity.util.TaskUtil;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

import java.util.Objects;

/**
 * A task responsible for periodically checking player security.
 *
 * @author KhanhHuynh1402
 */
public class PlayerSecurityChecker implements Runnable {
    @Getter
    private final IPSecurityPlugin plugin;
    @Getter
    private final int taskID;
    private final DiscordBot bot;
    private final Settings settings;

    /**
     * Constructs a new PlayerSecurityChecker.
     *
     * @param plugin The {@link IPSecurityPlugin} instance.
     */
    public PlayerSecurityChecker(IPSecurityPlugin plugin) {
        this.plugin = plugin;
        this.bot = plugin.getDiscordBot();
        this.settings = plugin.getSettings();

        taskID = Bukkit.getScheduler().runTaskTimerAsynchronously(
                plugin,
                this,
                0,
                plugin.getSettings().getProtectInterval()
        ).getTaskId();
    }

    /**
     * Runs the player security check.
     */
    @Override
    public void run() {
        for (Player player: Bukkit.getOnlinePlayers()) {
            if (!isValidPlayer(player)) {
                handleInvalidPlayer(player);
            }
        }
    }


    /**
     * Checks if the player is valid based on security settings.
     *
     * @param player The {@link Player} to check.
     * @return True if the player is valid, otherwise false.
     */
    public boolean isValidPlayer(Player player) {
        return (!settings.isCheckOp() || checkOp(player)) &&
                (!settings.isCheckGamemode() || checkGamemode(player)) &&
                checkPermissions(player);
    }

    /**
     * Handles an invalid player by kicking them from the server.
     *
     * @param player The {@link Player} to handle.
     */
    public void handleInvalidPlayer(Player player) {
        TaskUtil.runSync(() -> {
            if (player.isOnline()) {
                Messages messages = plugin.getMessages();

                String kickMessage = ChatColor.translateAlternateColorCodes('&', messages.getKickMessage());
                player.kickPlayer(kickMessage);
                bot.sendNotification(messages.getInvalidMessageEmbed(player));
            }
        });
    }

    /**
     * Checks if the player has op permissions and if their IP matches.
     *
     * @param player The {@link Player} to check.
     * @return True if the player passes the check, otherwise false.
     */
    private boolean checkOp(Player player) {
        if (!player.isOp()) {
            return true;
        }
        String ip = plugin.getDataStorage().getPlayerIP(player).join();
        return Objects.requireNonNull(
                player.getAddress(),
                "Unable to get " + player.getName() + "'s IP address"
        ).getHostString().equals(ip);
    }

    /**
     * Checks if the player's gamemode is creative and if their IP matches.
     *
     * @param player The {@link Player} to check.
     * @return True if the player passes the check, otherwise false.
     */
    private boolean checkGamemode(Player player) {
        if (!player.getGameMode().equals(GameMode.CREATIVE)) {
            return true;
        }
        String ip = plugin.getDataStorage().getPlayerIP(player).join();
        return Objects.requireNonNull(
                player.getAddress(),
                "Unable to get " + player.getName() + "'s IP address"
        ).getHostString().equals(ip);
    }

    /**
     * Checks if the player has any specified permissions and if their IP matches.
     *
     * @param player The {@link Player} to check.
     * @return True if the player passes the check, otherwise false.
     */
    private boolean checkPermissions(Player player) {
        for (String perm : settings.getCheckPermissions()) {
            if (player.hasPermission(perm)) {
                String ip = plugin.getDataStorage().getPlayerIP(player).join();
                return Objects.requireNonNull(
                        player.getAddress(),
                        "Unable to get " + player.getName() + "'s IP address"
                ).getHostString().equals(ip);
            }
        }
        return true;
    }

    /**
     * Check if player should be validated.
     *
     * @param player The {@link Player} need check.
     * @return true if the player needs to be validated, false otherwise.
     */
    public boolean isShouldValidate(Player player) {
        if (settings.isCheckOp() && player.isOp()) {
            return true;
        }

        if (settings.isCheckGamemode() && player.getGameMode().equals(GameMode.CREATIVE)) {
            return true;
        }

        for (String perm: settings.getCheckPermissions()) {
            if (player.hasPermission(perm)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Cancels the task.
     */
    public void cancel() {
        Bukkit.getScheduler().cancelTask(taskID);
    }
}
