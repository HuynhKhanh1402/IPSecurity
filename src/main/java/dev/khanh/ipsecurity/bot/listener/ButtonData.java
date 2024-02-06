package dev.khanh.ipsecurity.bot.listener;

import lombok.Getter;

import java.util.UUID;

public class ButtonData {
    @Getter
    private final UUID buttonUUID;
    @Getter
    private final String playerName;
    @Getter
    private final String ip;

    public ButtonData(UUID buttonUUID, String playerName, String ip) {
        this.buttonUUID = buttonUUID;
        this.playerName = playerName;
        this.ip = ip;
    }
}
