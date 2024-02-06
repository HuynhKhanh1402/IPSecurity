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
        return compareVersions(getCurrentServerVersion(), version) >= 0;
    }

    /**
     * Compares two version strings numerically, considering each component separated by periods.
     *
     * @param version1 First version string to compare.
     * @param version2 Second version string to compare.
     * @return 0 if equal, negative if version1 is less, positive if version1 is greater.
     * @throws NumberFormatException If version strings contain non-numeric components.
     */
    public static int compareVersions(String version1, String version2) {
        int comparisonResult = 0;

        String[] version1Splits = version1.split("\\.");
        String[] version2Splits = version2.split("\\.");
        int maxLengthOfVersionSplits = Math.max(version1Splits.length, version2Splits.length);

        for (int i = 0; i < maxLengthOfVersionSplits; i++){
            Integer v1 = i < version1Splits.length ? Integer.parseInt(version1Splits[i]) : 0;
            Integer v2 = i < version2Splits.length ? Integer.parseInt(version2Splits[i]) : 0;
            int compare = v1.compareTo(v2);
            if (compare != 0) {
                comparisonResult = compare;
                break;
            }
        }
        return comparisonResult;
    }

}
