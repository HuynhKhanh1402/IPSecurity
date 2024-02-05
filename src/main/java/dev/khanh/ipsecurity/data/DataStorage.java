package dev.khanh.ipsecurity.data;

import org.bukkit.OfflinePlayer;

import java.util.concurrent.CompletableFuture;

/**
 * Interface for handling data storage
 *
 * @author KhanhHuynh1402
 */
public interface DataStorage {

    /**
     * Sets the IP address associated with the specified player.
     *
     * @param player The offline player
     * @param ip     The IP address to set
     * @return A CompletableFuture representing the asynchronous operation result
     */
    CompletableFuture<Void> setPlayerIP(OfflinePlayer player, String ip);

    /**
     * Retrieves the IP address associated with the specified player.
     *
     * @param player The offline player
     * @return A CompletableFuture representing the asynchronous operation result, containing the player's IP address
     */
    CompletableFuture<String> getPlayerIP(OfflinePlayer player);

    /**
     * Removes the IP address associated with the specified player.
     *
     * @param player The offline player
     * @return A CompletableFuture representing the asynchronous operation result, indicating whether the removal was successful
     */
    CompletableFuture<Boolean> removePlayerIP(OfflinePlayer player);

    /**
     * Shuts down the data storage system.
     */
    void shutdown();
}
