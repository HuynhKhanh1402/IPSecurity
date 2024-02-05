package dev.khanh.ipsecurity.util;

import dev.khanh.ipsecurity.IPSecurityPlugin;
import dev.khanh.ipsecurity.file.Settings;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.bukkit.entity.Player;

import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility class for parsing placeholders in MessageEmbeds.
 *
 * @author KhanhHuynh1402
 */
public class MessageEmbedUtil {
    /**
     * Parses placeholders in a MessageEmbed using player-specific placeholders.
     *
     * @param origin The original MessageEmbed.
     * @param player The player to parse placeholders for.
     * @return The MessageEmbed with placeholders replaced.
     */
    public static MessageEmbed parsePlaceholder(MessageEmbed origin, Player player) {
        return parsePlaceholder(
                origin,
                player.getName(),
                player.getAddress() == null ? "N/a" : player.getAddress().getAddress().getHostAddress()
        );
    }

    /**
     * Parses placeholders in a MessageEmbed using player name placeholder.
     *
     * @param origin     The original MessageEmbed.
     * @param playerName The player name.
     * @return The MessageEmbed with placeholders replaced.
     */
    public static MessageEmbed parsePlaceholder(MessageEmbed origin, String playerName) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%player%", playerName);
        placeholders.put("%time%", getCurrentTime());

        return parsePlaceholder(origin, placeholders);
    }

    /**
     * Parses placeholders in a MessageEmbed using player-specific placeholders.
     *
     * @param origin     The original MessageEmbed.
     * @param playerName The player name.
     * @param ip         The player IP address.
     * @return The MessageEmbed with placeholders replaced.
     */
    public static MessageEmbed parsePlaceholder(MessageEmbed origin, String playerName, String ip) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("%player%", playerName);
        placeholders.put("%ip%", ip);
        placeholders.put("%time%", getCurrentTime());

        return parsePlaceholder(origin, placeholders);
    }

    /**
     * Parses time placeholder in a MessageEmbed.
     *
     * @param origin The original MessageEmbed.
     * @return The MessageEmbed with placeholders replaced.
     */
    public static MessageEmbed parseTimePlaceholder(MessageEmbed origin) {
        return parsePlaceholder(origin, Collections.singletonMap("%time%", getCurrentTime()));
    }

    public static MessageEmbed parsePlaceholder(MessageEmbed messageEmbed, Map<String, String> placeholders) {
        EmbedBuilder builder = new EmbedBuilder(messageEmbed);

        String title = messageEmbed.getTitle();
        if (title != null) {
            builder.setTitle(replacePlaceholders(title, placeholders));
        }

        String description = messageEmbed.getDescription();
        if (description != null) {
            builder.setDescription(replacePlaceholders(description, placeholders));
        }

        MessageEmbed.AuthorInfo authorInfo = messageEmbed.getAuthor();
        if (authorInfo != null && authorInfo.getName() != null) {
            builder.setAuthor(
                    replacePlaceholders(authorInfo.getName(), placeholders),
                    authorInfo.getUrl(),
                    authorInfo.getIconUrl()
            );
        }

        String footerText = messageEmbed.getFooter() != null ? messageEmbed.getFooter().getText() : null;
        if (footerText != null) {
            builder.setFooter(replacePlaceholders(footerText, placeholders));
        }

        return builder.build();
    }

    private static String replacePlaceholders(String text, Map<String, String> placeholders) {
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            text = text.replaceAll(entry.getKey(), entry.getValue());
        }
        return text;
    }

    private static String getCurrentTime() {
        Settings settings = IPSecurityPlugin.getInstance().getSettings();
        return settings.getDateFormat().format(Calendar.getInstance(settings.getTimeZone()).getTime());
    }
}
