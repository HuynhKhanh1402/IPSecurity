package dev.khanh.ipsecurity.command;

import dev.khanh.ipsecurity.IPSecurityPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * Represents the command executor and tab completer for IPSecurity plugin commands.
 * This class handles the execution of plugin commands and tab completion for command arguments.
 *
 * @author KhanhHuynh1402
 */
public class IPSecurityCommand implements CommandExecutor, TabCompleter {
    private final IPSecurityPlugin plugin;

    /**
     * Constructs a new IPSecurityCommand.
     *
     * @param plugin The IPSecurityPlugin instance.
     */
    public IPSecurityCommand(IPSecurityPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Executes the IPSecurity command.
     *
     * @param sender  The command sender.
     * @param command The command being executed.
     * @param label   The alias used for the command.
     * @param args    The arguments passed with the command.
     * @return true if the command was handled successfully, otherwise false.
     */
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ChatColor.RED.toString() + ChatColor.BOLD + "IPSecurity");
            sender.sendMessage("Author: " + ChatColor.YELLOW + "KhanhHuynh");
            sender.sendMessage("Version: " + ChatColor.YELLOW + plugin.getDescription().getVersion());
            return true;
        }

        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            sender.sendMessage(ChatColor.RED + "The reload command is only effective for messages. Other settings require a server restart to work.");
            plugin.reloadMessages();
            sender.sendMessage(ChatColor.GREEN + "Reloaded messages.");
            return true;
        }

        return false;
    }

    /**
     * Provides tab completion for IPSecurity commands.
     *
     * @param sender  The command sender.
     * @param command The command being completed.
     * @param alias   The alias used for the command.
     * @param args    The arguments passed with the command so far.
     * @return A list of tab completions for the current argument, or an empty list if there are no completions.
     */
    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1 && "reload".startsWith(args[0])) {
            return Collections.singletonList("reload");
        }
        return Collections.emptyList();
    }
}
