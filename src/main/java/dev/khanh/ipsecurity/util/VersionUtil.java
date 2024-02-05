package dev.khanh.ipsecurity.util;

import org.bukkit.Bukkit;

/**
 * Utility class for version comparison.
 *
 * @author KhanhHuynh1402
 */
public class VersionUtil {

    /**
     * Retrieves the current version of the Bukkit server.
     *
     * @return The current version of the Bukkit server.
     */
    private static String getCurrentServerVersion() {
        return Bukkit.getBukkitVersion().split("-")[0];
    }

    /**
     * Checks if the current server version is newer than the specified version.
     *
     * @param version The version to compare against.
     * @return True if the current server version is newer than or equal to the specified version, otherwise false.
     */
    public static boolean isCurrentServerVersionNewerOrEqual(String version) {
        int comparisonResult = 0;

        String[] currentVersionSplits = getCurrentServerVersion().split("\\.");
        String[] specifiedVersionSplits = version.split("\\.");
        int maxLengthOfVersionSplits = Math.max(currentVersionSplits.length, specifiedVersionSplits.length);

        for (int i = 0; i < maxLengthOfVersionSplits; i++) {
            Integer currentVersionPart = i < currentVersionSplits.length ? Integer.parseInt(currentVersionSplits[i]) : 0;
            Integer specifiedVersionPart = i < specifiedVersionSplits.length ? Integer.parseInt(specifiedVersionSplits[i]) : 0;
            int compare = currentVersionPart.compareTo(specifiedVersionPart);
            if (compare != 0) {
                comparisonResult = compare;
                break;
            }
        }
        return comparisonResult >= 0;
    }
}
