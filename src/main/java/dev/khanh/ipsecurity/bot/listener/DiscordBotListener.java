package dev.khanh.ipsecurity.bot.listener;

import dev.khanh.ipsecurity.IPSecurityPlugin;
import dev.khanh.ipsecurity.bot.DiscordBot;
import dev.khanh.ipsecurity.file.Messages;
import lombok.Getter;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Listener class for Discord bot interactions with the server plugin.
 * This class handles slash command interactions from Discord and performs actions related to IP protection for the server.
 *
 * @author KhanhHuynh1402
 */
public class DiscordBotListener extends ListenerAdapter {
    @Getter
    private final DiscordBot bot;
    private final IPSecurityPlugin plugin;
    private final Pattern IP_PATTERN = Pattern.compile("^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$");
    private final Map<String, ButtonData> cachedButtons = new HashMap<>();


    /**
     * Constructs a new DiscordBotListener.
     *
     * @param bot The {@link DiscordBot} instance.
     */
    public DiscordBotListener(DiscordBot bot) {
        this.bot = bot;
        this.plugin = bot.getPlugin();
    }

    /**
     * Handles slash command interactions from Discord.
     *
     * @param event The SlashCommandInteractionEvent.
     */
    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Messages messages = plugin.getMessages();

        Member member = event.getMember();

        InteractionHook hook = event.getHook();

        if (bot.getRole() == null || member == null || !member.getRoles().contains(bot.getRole())){
            event.deferReply(true).queue();
            hook.sendMessageEmbeds(messages.getNoPermissionMessageEmbed()).queue();
            return;
        }

        if (!event.getChannel().equals(bot.getChannel())){
            event.deferReply(true).queue();
            hook.sendMessageEmbeds(messages.getWrongChannelMessageEmbed()).queue();
            return;
        }

        if (event.getSubcommandName() == null){
            if (event.getOptions().size() != 2){
                event.deferReply(true).queue();
                hook.sendMessageEmbeds(messages.getInvalidSyntaxMessageEmbed()).queue();
                return;
            }
        }

        if (event.getSubcommandName().equals("set")){
            handleSetCommand(event, messages);
        }

        if (event.getSubcommandName().equals("remove")){
            handleRemoveCommand(event, messages);
        }
    }

    /**
     * Handles buttons interactions from Discord.
     *
     * @param event The {@link ButtonInteractionEvent}.
     */
    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Button button = event.getButton();
        Member member = event.getMember();

        Messages messages = plugin.getMessages();

        ButtonData buttonData = cachedButtons.get(button.getId());

        if (buttonData == null) {
            return;
        }

        if (bot.getRole() == null || member == null || !member.getRoles().contains(bot.getRole())){
            event.deferReply(true).queue();
            event.getHook().sendMessageEmbeds(messages.getNoPermissionMessageEmbed()).queue();
            return;
        }

        String player = buttonData.getPlayerName();
        String ip = buttonData.getIp();

        event.deferReply(false).queue();

        plugin.getDataStorage().setPlayerIP(buttonData.getPlayerName(), buttonData.getIp()).thenRunAsync(() -> {
            event.getHook().sendMessageEmbeds(messages.getSetIpSuccessfulMessageEmbed(player, ip)).queue();
        }).exceptionally(throwable -> {
            event.getHook().sendMessageEmbeds(messages.getSetIpFailedMessageEmbed(player, ip)).queue();
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        });
    }

    /**
     * Handle the remove command
     * @param event The {@link SlashCommandInteractionEvent}
     * @param messages The {@link Messages} plugin messages
     */
    private void handleSetCommand(SlashCommandInteractionEvent event, Messages messages) {
        if (event.getOptions().size() != 2){
            event.deferReply(true).queue();
            event.getHook().sendMessageEmbeds(messages.getInvalidSyntaxMessageEmbed()).queue();
            return;
        }

        if (!isValidIP(Objects.requireNonNull(event.getOption("ip")).getAsString())){
            event.deferReply(true).queue();
            event.getHook().sendMessageEmbeds(messages.getInvalidIpFormatMessageEmbed()).queue();
            return;
        }

        String player = Objects.requireNonNull(event.getOption("player")).getAsString();
        String ip = Objects.requireNonNull(event.getOption("ip")).getAsString();

        event.deferReply(false).queue();

        plugin.getDataStorage().setPlayerIP(player, ip).thenRunAsync(() -> {
            event.getHook().sendMessageEmbeds(messages.getSetIpSuccessfulMessageEmbed(player, ip)).queue();
        }).exceptionally(throwable -> {
            event.getHook().sendMessageEmbeds(messages.getSetIpFailedMessageEmbed(player, ip)).queue();
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        });
    }

    /**
     * Handle the remove command
     * @param event The {@link SlashCommandInteractionEvent}
     * @param messages The {@link Messages} plugin messages
     */
    private void handleRemoveCommand(SlashCommandInteractionEvent event, Messages messages) {
        if (event.getOptions().size() != 1) {
            event.deferReply(false).queue();
            event.getHook().sendMessageEmbeds(messages.getInvalidSyntaxMessageEmbed()).queue();
            return;
        }

        String player = Objects.requireNonNull(event.getOption("player")).getAsString();

        event.deferReply(false).queue();

        plugin.getDataStorage().removePlayerIP(player).thenAcceptAsync(flag -> {
            if (flag) {
                event.getHook().sendMessageEmbeds(messages.getRemoveIpSuccessfulMessageEmbed(player)).queue();
            } else {
                event.getHook().sendMessageEmbeds(messages.getNotFoundPlayerMessageEmbed(player)).queue();
            }
        }).exceptionally(throwable -> {
            event.getHook().sendMessageEmbeds(messages.getRemoveIpFailedMessageEmbed(player)).queue();
            throwable.printStackTrace();
            throw new RuntimeException(throwable);
        });
    }

    /**
     * Checks if the provided IP address is valid.
     *
     * @param ip The IP address to validate.
     * @return true if the IP address is valid, otherwise false.
     */
    private boolean isValidIP(String ip) {
        Matcher matcher = IP_PATTERN.matcher(ip);
        return matcher.matches();
    }

    /**
     * Register the button handle event
     * @param buttonData the {@link ButtonData}
     */
    public void registerButtonListener(ButtonData buttonData) {
        cachedButtons.put(buttonData.getButtonUUID().toString(), buttonData);
    }
}
