package dev.khanh.ipsecurity.data;

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
     * @param playerName The name of player
     * @param ip     The IP address to set
     * @return A CompletableFuture representing the asynchronous operation result
     */
    CompletableFuture<Void> setPlayerIP(String playerName, String ip);

    /**
     * Retrieves the IP address associated with the specified player.
     *
     * @param playerName The name of player
     * @return A CompletableFuture representing the asynchronous operation result, containing the player's IP address
     */
    CompletableFuture<String> getPlayerIP(String playerName);

    /**
     * Removes the IP address associated with the specified player.
     *
     * @param playerName The name of player
     * @return A CompletableFuture representing the asynchronous operation result, indicating whether the removal was successful
     */
    CompletableFuture<Boolean> removePlayerIP(String playerName);

    /**
     * Shuts down the data storage system.
     */
    void shutdown();
}
