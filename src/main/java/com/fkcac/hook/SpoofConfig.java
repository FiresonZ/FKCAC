package com.fkcac.hook;

import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.config.Configuration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * User-tweakable behaviour of the bypass. Mirrors the role of
 * {@code config/antiantiCheat.cfg} in Anti-AntiCheat-MOD:
 * the MD5/hash list sent back to the server can be pinned so the client can keep
 * arbitrary extra mods without the server flagging them.
 */
public final class SpoofConfig {
    private static Configuration config;

    private static String[] customHashes = new String[0];
    private static boolean spoofScreenshot = true;
    private static boolean hideAllQueriedClasses = true;

    private SpoofConfig() { }

    public static void load(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());

        spoofScreenshot = config.get(Configuration.CATEGORY_GENERAL,
                "spoofScreenshot", true,
                "Whether to answer screenshot requests with a blank PNG.").getBoolean();
        hideAllQueriedClasses = config.get(Configuration.CATEGORY_GENERAL,
                "hideAllQueriedClasses", true,
                "Whether to report every class check as 'not found'.").getBoolean();

        customHashes = config.get(Configuration.CATEGORY_GENERAL,
                "fileHashList", new String[0],
                "MD5/size hashes to report for the file check, one per line.").getStringList();

        config.setCategoryComment(Configuration.CATEGORY_GENERAL,
                "FKCAC bypass behaviour. Edits take effect on the next reload / restart.");
        config.save();
    }

    /** Rotate the salt returned with responses (currently a pass-through). */
    public static byte refreshSalt(byte currentSalt) {
        return currentSalt;
    }

    /** The hash list reported to the server on a file check. */
    public static List<String> fileHashList() {
        if (customHashes == null || customHashes.length == 0) {
            // A single stable, whitelisted-looking hash keeps the server happy.
            return Collections.singletonList("1.7.10=" + String.valueOf(Math.abs("1.7.10".hashCode())));
        }
        return new ArrayList<String>(Arrays.asList(customHashes));
    }

    /** Which of the queried classes should be reported as present (all hidden by default). */
    public static List<String> filterFoundClasses(List<String> queried) {
        if (hideAllQueriedClasses) {
            return Collections.emptyList();
        }
        return queried == null ? Collections.<String>emptyList() : queried;
    }

    public static boolean spoofScreenshot() {
        return spoofScreenshot;
    }
}