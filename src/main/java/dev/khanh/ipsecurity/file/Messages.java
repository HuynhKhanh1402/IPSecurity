package dev.khanh.ipsecurity.file;

import com.google.common.base.Preconditions;
import dev.khanh.ipsecurity.IPSecurityPlugin;
import dev.khanh.ipsecurity.util.MessageEmbedUtil;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.apache.commons.lang.StringUtils;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.io.File;
import java.io.IOException;

/**
 * Manages messages and embed messages
 * This class loads messages from a YAML configuration file and provides methods to retrieve message embeds.
 *
 * @author KhanhHuynh1402
 */
public class Messages {
    @Getter
    private final File file;
    @Getter
    private final YamlConfiguration yaml;
    @Getter
    private String kickMessage;
    @Getter
    private String verifiedMessage;
    @Getter
    private String noPermissionMessage;
    private MessageEmbed verifiedMessageEmbed;
    private MessageEmbed invalidMessageEmbed;
    private MessageEmbed noPermissionMessageEmbed;
    private MessageEmbed wrongChannelMessageEmbed;
    private MessageEmbed invalidSyntaxMessageEmbed;
    private MessageEmbed invalidIpFormatMessageEmbed;
    private MessageEmbed setIpSuccessfulMessageEmbed;
    private MessageEmbed setIpFailedMessageEmbed;
    private MessageEmbed removeIpSuccessfulMessageEmbed;
    private MessageEmbed removeIpFailedMessageEmbed;
    private MessageEmbed notFoundPlayerMessageEmbed;

    /**
     * Constructs a new Messages instance.
     *
     * @param plugin The IPSecurityPlugin instance.
     */
    public Messages(IPSecurityPlugin plugin) {
        try {

            file = new File(plugin.getDataFolder(), "messages.yml");

            if (!file.exists()) {
                plugin.saveResource("messages.yml", true);
            }

            yaml = YamlConfiguration.loadConfiguration(file);

            loadIngameMessages();

            loadDiscordMessages();

        } catch (Exception e) {
            throw new RuntimeException("An error occurred while loading the messages.yml file", e);
        }
    }

    /**
     * Saves the current message configuration to the file.
     *
     * @throws IOException If an error occurs while saving the configuration.
     */
    public void save() throws IOException {
        yaml.save(file);
    }

    private void loadIngameMessages() {
        kickMessage = yaml.getString("minecraft.kick-message");
        verifiedMessage = yaml.getString("minecraft.verified");
        noPermissionMessage = yaml.getString("minecraft.no-permission");
    }

    private void loadDiscordMessages() {
        MessageEmbed defaultMessage;

        try {
            ConfigurationSection section = yaml.getConfigurationSection("discord.default-embed");
            Preconditions.checkNotNull(section, "[messages.yml] discord.default-embed is null");

            defaultMessage = buildEmbedFromSection(section, null);
        } catch (Exception e) {
            throw new RuntimeException("An error occurred while loading message: discord.default-embed");
        }

        verifiedMessageEmbed = loadMessageEmbed(defaultMessage, "verified");
        invalidMessageEmbed = loadMessageEmbed(defaultMessage, "verified-failed");
        noPermissionMessageEmbed = loadMessageEmbed(defaultMessage, "no-permission");
        wrongChannelMessageEmbed = loadMessageEmbed(defaultMessage, "wrong-channel");
        invalidSyntaxMessageEmbed = loadMessageEmbed(defaultMessage, "invalid-syntax");
        invalidIpFormatMessageEmbed = loadMessageEmbed(defaultMessage, "invalid-ip-format");
        setIpSuccessfulMessageEmbed = loadMessageEmbed(defaultMessage, "set-ip-successful");
        setIpFailedMessageEmbed = loadMessageEmbed(defaultMessage, "set-ip-failed");
        removeIpSuccessfulMessageEmbed = loadMessageEmbed(defaultMessage, "remove-ip-successful");
        removeIpFailedMessageEmbed = loadMessageEmbed(defaultMessage, "remove-ip-failed");
        notFoundPlayerMessageEmbed = loadMessageEmbed(defaultMessage, "not-found-player");

    }

    private MessageEmbed buildEmbedFromSection(@NotNull ConfigurationSection section, @Nullable MessageEmbed defaultMessage) {
        EmbedBuilder builder = new EmbedBuilder(defaultMessage);

        if (section.contains("title")) {
            builder.setTitle(section.getString("title"));
        }

        if (section.contains("color")) {
            String color = section.getString("color", "");
            try {
                assert color != null;
                builder.setColor(Color.decode(color));
            } catch (NumberFormatException e) {
                throw new RuntimeException("Invalid color code: " + color);
            }

        }

        builder.setDescription(StringUtils.join(section.getStringList("messages"), "\n"));

        if (section.contains("author")) {
            String name = section.getString("author.name", "");
            String url = section.getString("author.url", "");
            String icon = section.getString("author.icon", "");

            builder.setAuthor(name, url, icon);
        }

        if (section.contains("footer")) {
            builder.setFooter(section.getString("footer", ""));
        }

        if (section.contains("image")) {
            builder.setImage(section.getString("image"));
        }

        if (section.contains("thumbnail")) {
            builder.setThumbnail(section.getString("thumbnail"));
        }

        return builder.build();
    }

    private MessageEmbed loadMessageEmbed(MessageEmbed defaultMessage, String message) {
        try {

            ConfigurationSection section = yaml.getConfigurationSection("discord.messages." + message);
            Preconditions.checkNotNull(section, "[messages.yml] discord.messages.%s is null", message);
            return buildEmbedFromSection(section, defaultMessage);

        } catch (Exception e) {
            throw new RuntimeException("An error occurred while loading message: discord.messages." + message);
        }
    }

    /**
     * Retrieves the message embed for notifying a player that they have been verified.
     *
     * @param player The player to notify.
     * @return The message embed.
     */
    public MessageEmbed getVerifiedMessageEmbed(Player player) {
        return MessageEmbedUtil.parsePlaceholder(verifiedMessageEmbed, player);
    }

    /**
     * Retrieves the message embed for notifying a player that verification failed.
     *
     * @param player The player to notify.
     * @return The message embed.
     */
    public MessageEmbed getInvalidMessageEmbed(Player player) {
        return MessageEmbedUtil.parsePlaceholder(invalidMessageEmbed, player);
    }

    /**
     * Retrieves the message embed for notifying a player that their IP has been successfully set.
     *
     * @param playerName The name of the player.
     * @param ip         The IP address.
     * @return The message embed.
     */
    public MessageEmbed getSetIpSuccessfulMessageEmbed(String playerName, String ip) {
        return MessageEmbedUtil.parsePlaceholder(setIpSuccessfulMessageEmbed, playerName, ip);
    }

    /**
     * Retrieves the message embed for notifying a player that setting their IP failed.
     *
     * @param playerName The name of the player.
     * @param ip         The IP address.
     * @return The message embed.
     */
    public MessageEmbed getSetIpFailedMessageEmbed(String playerName, String ip) {
        return MessageEmbedUtil.parsePlaceholder(setIpFailedMessageEmbed, playerName, ip);
    }

    /**
     * Retrieves the message embed for notifying a player that their IP has been successfully removed.
     *
     * @param playerName The name of the player.
     * @return The message embed.
     */
    public MessageEmbed getRemoveIpSuccessfulMessageEmbed(String playerName) {
        return MessageEmbedUtil.parsePlaceholder(removeIpSuccessfulMessageEmbed, playerName, null);
    }

    /**
     * Retrieves the message embed for notifying a player that removing their IP failed.
     *
     * @param playerName The name of the player.
     * @return The message embed.
     */
    public MessageEmbed getRemoveIpFailedMessageEmbed(String playerName) {
        return MessageEmbedUtil.parsePlaceholder(removeIpFailedMessageEmbed, playerName, null);
    }

    /**
     * Retrieves the message embed for notifying a player that they could not be found.
     *
     * @param playerName The name of the player.
     * @return The message embed.
     */
    public MessageEmbed getNotFoundPlayerMessageEmbed(String playerName) {
        return MessageEmbedUtil.parsePlaceholder(notFoundPlayerMessageEmbed, playerName);
    }

    /**
     * Retrieves the message embed for notifying that the player has no permission.
     *
     * @return The message embed.
     */
    public MessageEmbed getNoPermissionMessageEmbed() {
        return MessageEmbedUtil.parseTimePlaceholder(noPermissionMessageEmbed);
    }

    /**
     * Retrieves the message embed for notifying that the command was used in the wrong channel.
     *
     * @return The message embed.
     */
    public MessageEmbed getWrongChannelMessageEmbed() {
        return MessageEmbedUtil.parseTimePlaceholder(wrongChannelMessageEmbed);
    }

    /**
     * Retrieves the message embed for notifying that the command syntax is invalid.
     *
     * @return The message embed.
     */
    public MessageEmbed getInvalidSyntaxMessageEmbed() {
        return MessageEmbedUtil.parseTimePlaceholder(invalidSyntaxMessageEmbed);
    }

    /**
     * Retrieves the message embed for notifying that the provided IP format is invalid.
     *
     * @return The message embed.
     */
    public MessageEmbed getInvalidIpFormatMessageEmbed() {
        return MessageEmbedUtil.parseTimePlaceholder(invalidIpFormatMessageEmbed);
    }
}
