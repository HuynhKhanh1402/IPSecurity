package dev.khanh.ipsecurity.bot;

import com.google.common.base.Preconditions;
import dev.khanh.ipsecurity.IPSecurityPlugin;
import dev.khanh.ipsecurity.bot.listener.DiscordBotListener;
import dev.khanh.ipsecurity.util.PluginLogger;
import dev.khanh.ipsecurity.util.TaskUtil;
import lombok.Getter;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Represents a Discord bot instance used for interactions with the server plugin.
 * This bot handles communication between the server and Discord, including command handling and notifications.
 *
 * @author KhanhHuynh1402
 */
@Getter
public class DiscordBot {
    private final IPSecurityPlugin plugin;
    private final JDA jda;
    private final Guild guild;
    private final Role role;
    private final TextChannel channel;

    /**
     * Constructs a new DiscordBot instance.
     *
     * @param plugin The IPSecurityPlugin instance.
     */
    public DiscordBot(IPSecurityPlugin plugin) {
        this.plugin = plugin;

        ConfigurationSection section = plugin.getSettings().getConfig().getConfigurationSection("discord");
        Preconditions.checkNotNull(section, "[config.yml] discord section is null");

        String token = section.getString("token");

        jda = JDABuilder.createDefault(token)
                .addEventListeners(new DiscordBotListener(this))
                .build();

        PluginLogger.info("Initializing discord bot...");
        try {
            jda.awaitReady();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        String guildID = section.getString("guild");
        Preconditions.checkNotNull(guildID, "[config.yml] discord.guild is null");
        guild = jda.getGuildById(guildID);
        Preconditions.checkNotNull(guild, "Couldn't find any guild with id: " + guildID);

        String roleID = section.getString("role");
        Preconditions.checkNotNull(roleID, "[config.yml] discord.role is null");
        role = jda.getRoleById(roleID);
        Preconditions.checkNotNull(role, "Couldn't find any role with id: " + roleID);

        String channelID = section.getString("notification-channel");
        Preconditions.checkNotNull(channelID, "[config.yml] discord.notification-channel is null");
        channel = guild.getTextChannelById(channelID);
        Preconditions.checkNotNull(channel, "Couldn't find any chat channel with id: " + channelID);

        // Run sync delayed task to avoid DiscordSRV deleting commands
        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            guild.updateCommands().addCommands(
                    Commands.slash("ipsecurity", "IPSecurity Commands")
                            .addSubcommands(new SubcommandData("set", "Set player's ip")
                                    .addOption(OptionType.STRING, "player", "Player's name")
                                    .addOption(OptionType.STRING, "ip", "Player's ip"))
                            .addSubcommands(new SubcommandData("remove", "Remove player's ip")
                                    .addOption(OptionType.STRING, "player", "Player's name"))
            ).onErrorMap(throwable -> {
                throwable.printStackTrace();
                throw new RuntimeException(throwable);
            }).onSuccess(commands -> PluginLogger.info("Registered slash commands")).queue();
        }, 10);

        PluginLogger.info("Successfully initialized discord bot");
    }

    /**
     * Sends a notification message to the configured channel.
     *
     * @param messageEmbed The message to send as an embed.
     */
    public void sendNotification(MessageEmbed messageEmbed) {
        TaskUtil.runAsync(() -> channel.sendMessageEmbeds(messageEmbed).queue());
    }

    /**
     * Shuts down the Discord bot.
     */
    public void shutdown() {
        if (jda != null) {
            try {
                jda.shutdown();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
