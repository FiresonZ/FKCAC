package com.fkcac.hook;

import com.fkcac.FKCAC;
import com.fkcac.network.ProtocolUtils;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import net.minecraftforge.common.config.Configuration;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * User-tweakable behaviour of the bypass, mirroring the role of
 * {@code config/antiantiCheat.cfg} in Anti-AntiCheat-MOD.
 *
 * <p>The file-hash list can be pinned so the server keeps whitelisting your current
 * mod set; when empty, FKCAC reports the real launch-source hashes (excluding its own
 * jar), which are on the server's allow-list for a healthy client.
 */
public final class SpoofConfig {
    private static Configuration config;

    private static String[] fileHashOverride = new String[0];
    private static boolean spoofScreenshot = true;

    private SpoofConfig() { }

    public static void load(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());

        spoofScreenshot = config.get(Configuration.CATEGORY_GENERAL,
                "spoofScreenshot", true,
                "Whether to answer screenshot requests with a blank PNG.").getBoolean();

        config.setCategoryComment(Configuration.CATEGORY_GENERAL,
                "FKCAC bypass behaviour. Each fileHashList entry is one line of "
                + "<40-hex-lowercase-SHA1>\\0<filename>; leave empty to auto-report "
                + "the real (allowed) launch sources. Edits take effect on reload.");

        fileHashOverride = config.get(Configuration.CATEGORY_GENERAL,
                "fileHashList", new String[0],
                "Hashes reported for the file check, one per line.").getStringList();

        config.save();
    }

    /** Rotate the salt returned with responses (currently a pass-through). */
    public static byte refreshSalt(byte currentSalt) {
        return currentSalt;
    }

    /** The hash list reported to the server on a file check (over one separately). */
    public static List<String> fileHashList() {
        if (fileHashOverride != null && fileHashOverride.length > 0) {
            return new ArrayList<String>(Arrays.asList(fileHashOverride));
        }
        // Default: real, allow-listed hashes of the client mods (excluding FKCAC itself).
        List<String> collected = ProtocolUtils.checkFile(selfJar());
        return collected.isEmpty() ? Collections.singletonList(defaultHash()) : collected;
    }

    public static boolean spoofScreenshot() {
        return spoofScreenshot;
    }

    private static File selfJar() {
        try {
            return new File(FKCAC.class.getProtectionDomain().getCodeSource().getLocation().getPath());
        } catch (Exception e) {
            return null;
        }
    }

    /** Fallback only used when nothing can be scanned (should never happen in-game). */
    private static String defaultHash() {
        String md5zeros = "0000000000000000000000000000000000000000";
        return md5zeros + "\0unknown";
    }
}