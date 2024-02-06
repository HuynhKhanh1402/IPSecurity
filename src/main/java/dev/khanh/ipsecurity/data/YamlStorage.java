package dev.khanh.ipsecurity.data;

import dev.khanh.ipsecurity.IPSecurityPlugin;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;

/**
 * Implementation of {@link DataStorage} interface for YAML file-based storage.
 *
 * @author KhanhHuynh1402
 */
public class YamlStorage implements DataStorage {
    @Getter
    private final IPSecurityPlugin plugin;
    @Getter
    private final File storageFile;
    @Getter
    private final YamlConfiguration yaml;


    /**
     * Constructor for YamlStorage.
     *
     * @param plugin The {@link IPSecurityPlugin} instance
     */
    public YamlStorage(IPSecurityPlugin plugin) {
        this.plugin = plugin;
        this.storageFile = new File(plugin.getDataFolder(), "data.yml");

        try {

            if (!storageFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                storageFile.createNewFile();
            }

            yaml = YamlConfiguration.loadConfiguration(storageFile);
        } catch (IOException e) {
            throw new RuntimeException("An error occurred while loading file data.yml", e);
        }
    }

    public void save() {
        try {
            yaml.save(storageFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CompletableFuture<Void> setPlayerIP(String playerName, String ip) {
        return CompletableFuture.runAsync(() -> {
            yaml.set(playerName, ip);
            save();
        });
    }

    @Override
    public CompletableFuture<String> getPlayerIP(String playerName) {
        return CompletableFuture.supplyAsync(() -> yaml.getString(playerName));
    }

    @Override
    public CompletableFuture<Boolean> removePlayerIP(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            boolean flag = yaml.contains(playerName);

            yaml.set(playerName, null);
            save();

            return flag;
        });
    }

    @Override
    public void shutdown() {
    }
}
