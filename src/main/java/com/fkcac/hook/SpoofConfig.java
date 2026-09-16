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
 * User-tweakable behaviour of the bypass.
 *
 * <p>Three levers are exposed:
 * <ul>
 *   <li>{@code protocolVersion} - must equal the (frozen) server-side
 *       {@code CatAntiCheat.version}; the open source plugin never changes it again.</li>
 *   <li>{@code fileHashList} - pinned {@code <40hexSHA1>\0<name>} lines reported for the
 *       file check; when empty, the real launch-source hashes (excluding FKCAC's own jar)
 *       are reported, which are on the server's allow-list for a healthy client.</li>
 *   <li>{@code trustedClasses} - the only classes reported as "found" on a class check.
 *       Defaults to the exact marker-class candidates the server picks from
 *       ({@code FMLUtils.fmlClasses}), so the server's marker is always reported present
 *       while any cheat/extra class is always reported absent.</li>
 *   <li>{@code spoofAuth} - whether to actively respond to the auth challenge-response
 *       protocol (discriminators 11–14). When enabled, FKCAC sends the client hello
 *       automatically after the handshake and answers server challenges with a valid
 *       SHA-1 digest computed from the hardcoded client salt.</li>
 *   <li>{@code spoofSecurityProfile} - whether to report a (structurally valid, neutral)
 *       security profile (discriminator 15) right after the handshake like the real
 *       client does.</li>
 * </ul>
 */
public final class SpoofConfig {
    private static Configuration config;

    private static int protocolVersion = 2;
    private static String[] fileHashOverride = new String[0];
    private static boolean spoofScreenshot = true;
    private static boolean spoofAuth = true;
    private static boolean spoofSecurityProfile = true;
    private static String[] trustedClasses = {
        // The server's marker-class candidates (see FMLUtils.fmlClasses):
        "net.minecraft.launchwrapper.ITweaker",
        "net.minecraft.launchwrapper.LaunchClassLoader",
        "ic2.core.IC2",
        "noppes.npcs.CustomNpcs",
        "slimeknights.tconstruct.TConstruct",
        "mekanism.common.Mekanism",
        "com.pixelmonmod.pixelmon.Pixelmon",
        "cpw.mods.ironchest.IronChest"
    };

    private SpoofConfig() { }

    public static void load(FMLPreInitializationEvent event) {
        config = new Configuration(event.getSuggestedConfigurationFile());

        protocolVersion = config.get(Configuration.CATEGORY_GENERAL,
                "protocolVersion", 2,
                "CatAntiCheat protocol version reported during the handshake; must equal "
                + "the server's CatAntiCheat.version (the frozen open-source value is 2).").getInt();

        spoofScreenshot = config.get(Configuration.CATEGORY_GENERAL,
                "spoofScreenshot", true,
                "Whether to answer screenshot requests with a blank PNG.").getBoolean();

        spoofAuth = config.get(Configuration.CATEGORY_GENERAL,
                "spoofAuth", true,
                "Whether to respond to the server auth challenge-response protocol "
                + "(discriminators 11–14). Requires the hardcoded client salt to be valid.").getBoolean();

        spoofSecurityProfile = config.get(Configuration.CATEGORY_GENERAL,
                "spoofSecurityProfile", true,
                "Whether to report a neutral security profile (discriminator 15) right "
                + "after the handshake, like the real CatAntiCheat client does.").getBoolean();

        config.setCategoryComment(Configuration.CATEGORY_GENERAL,
                "FKCAC bypass behaviour. Each fileHashList entry is one line of "
                + "<40-hex-uppercase-SHA1>\\0<filename>; leave empty to auto-report "
                + "the real (allowed) launch sources. Edits take effect on reload.");

        fileHashOverride = config.get(Configuration.CATEGORY_GENERAL,
                "fileHashList", new String[0],
                "Hashes reported for the file check, one per line.").getStringList();

        trustedClasses = config.get(Configuration.CATEGORY_GENERAL,
                "trustedClasses", trustedClasses,
                "Classes reported as found on a class check; everything else is hidden.").getStringList();

        config.save();
    }

    /** Rotate the salt returned with responses (currently a pass-through). */
    public static byte refreshSalt(byte currentSalt) {
        return currentSalt;
    }

    /** Protocol version reported to the server during the handshake. */
    public static int protocolVersion() {
        return protocolVersion;
    }

    /** Whether to actively participate in the auth challenge-response protocol. */
    public static boolean spoofAuth() {
        return spoofAuth;
    }

    /** Whether to report a security profile after the handshake. */
    public static boolean spoofSecurityProfile() {
        return spoofSecurityProfile;
    }

    /** The hash list reported to the server on a file check. */
    public static List<String> fileHashList() {
        if (fileHashOverride != null && fileHashOverride.length > 0) {
            return new ArrayList<String>(Arrays.asList(fileHashOverride));
        }
        // Default: real, allow-listed hashes of the client mods (excluding FKCAC itself).
        List<String> collected = ProtocolUtils.checkFile(selfJar());
        return collected.isEmpty() ? Collections.singletonList(defaultHash()) : collected;
    }

    /** Filter the queried classes down to the ones we pretend to have installed. */
    public static List<String> filterTrustedClasses(List<String> queried) {
        if (queried == null) {
            return Collections.emptyList();
        }
        List<String> found = new ArrayList<String>();
        for (String queriedClass : queried) {
            for (String trusted : trustedClasses) {
                if (trusted.equals(queriedClass)) {
                    found.add(queriedClass);
                    break;
                }
            }
        }
        return found;
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
        String zeros = "0000000000000000000000000000000000000000";
        return zeros + "\0unknown";
    }
}
