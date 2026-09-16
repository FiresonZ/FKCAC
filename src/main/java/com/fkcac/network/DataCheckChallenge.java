package com.fkcac.network;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * Mirrors {@code luohuayu.anticheat.DataCheckChallenge} from the server's modified
 * CatAntiCheat jar.
 *
 * <p>Two functions complete the {@code CPacketVanillaData} reply:
 * <ul>
 *   <li>{@link #buildRulesFingerprint}: digests the server-provided check rules and
 *       environment lists into the "rules" field.</li>
 *   <li>{@link #buildResponse}: binds the rules fingerprint, the nonce, and the
 *       reported vanilla flags into the {@code lf} response string.</li>
 * </ul>
 * Fields are appended as {@code key + '\0' + value + (byte)0xFF} (same field helper
 * as {@link HandshakeChallenge}); lists as {@code "<key>.size"} then one field per
 * element. Floats and booleans use {@code String.valueOf(...)} exactly like the
 * reference.
 */
public final class DataCheckChallenge {
    private DataCheckChallenge() { }

    /**
     * Build the vanilla-data check response string ({@code lf}).
     *
     * @param nonce             challenge nonce from {@code SPacketDataCheck}
     * @param rulesFingerprint  result of {@link #buildRulesFingerprint}
     * @param lighting          gamma &gt; 1.5 (brightness boost) flag
     * @param transparent       suspicious transparent-texture flag
     * @param texture           description of the (suspicious) texture, "" when clean
     * @return 40-char uppercase hex SHA-1 digest, or "ERROR" on failure
     */
    public static String buildResponse(int nonce, String rulesFingerprint, boolean lighting,
                                       boolean transparent, String texture) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            appendField(md, "nonce", String.valueOf(nonce));
            appendField(md, "rules", rulesFingerprint);
            appendField(md, "lighting", String.valueOf(lighting));
            appendField(md, "transparent", String.valueOf(transparent));
            appendField(md, "texture", texture);
            return toHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            return "ERROR";
        }
    }

    /**
     * Build the "rules" fingerprint from the server-provided check configuration.
     * All list/environment arguments come verbatim from {@code SPacketDataCheck}.
     */
    public static String buildRulesFingerprint(List<String> textures, boolean checkBrightness,
                                               float transparentThreshold, float brightnessThreshold,
                                               List<String> jvmArgs, List<String> systemProperties,
                                               List<String> propertyPatterns, List<String> classLoaders,
                                               List<String> threads) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            appendList(md, "textures", textures);
            appendField(md, "checkBrightness", String.valueOf(checkBrightness));
            appendField(md, "transparentThreshold", String.valueOf(transparentThreshold));
            appendField(md, "brightnessThreshold", String.valueOf(brightnessThreshold));
            appendList(md, "envJvmArgs", jvmArgs);
            appendList(md, "envSystemProperties", systemProperties);
            appendList(md, "envPropertyPatterns", propertyPatterns);
            appendList(md, "envClassLoaders", classLoaders);
            appendList(md, "envThreads", threads);
            return toHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            return "ERROR";
        }
    }

    private static void appendField(MessageDigest md, String key, String value) {
        md.update(key.getBytes(StandardCharsets.UTF_8));
        md.update('\0');
        md.update((value != null ? value : "").getBytes(StandardCharsets.UTF_8));
        md.update((byte) 0xFF);
    }

    private static void appendList(MessageDigest md, String key, List<String> list) {
        int size = list != null ? list.size() : 0;
        appendField(md, key + ".size", String.valueOf(size));
        if (list == null) {
            return;
        }
        for (String item : list) {
            appendField(md, key, item);
        }
    }

    private static String toHex(byte[] digest) {
        StringBuilder sb = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}