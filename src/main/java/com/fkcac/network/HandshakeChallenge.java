package com.fkcac.network;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Mirrors {@code luohuayu.anticheat.HandshakeChallenge} from the server's CatAntiCheat jar.
 *
 * <p>Computes a SHA-1 hex digest over a structured challenge payload. Every field is
 * appended as {@code key + '\0' + value + (byte)0xFF} (the trailing 0xFF separator is
 * what the original {@code a(MessageDigest, String, String)} helper emits via
 * {@code MessageDigest.update((byte)-1)}):
 * <pre>
 *   "nonce"        + '\0' + str(nonce)        + 0xFF
 *   "salt"         + '\0' + str(salt & 0xFF)  + 0xFF
 *   "flags"        + '\0' + str(flags)        + 0xFF
 *   [if flags & 0x01] "version"     + '\0' + str(version)       + 0xFF
 *   [if flags & 0x02] "integrity"   + '\0' + integrityHex      + 0xFF
 *   [if flags & 0x04] "classSource" + '\0' + classSourceHex    + 0xFF
 * </pre>
 * The result is a 40-character uppercase hex string.
 */
public final class HandshakeChallenge {
    public static final int FLAG_VERSION = 0x01;
    public static final int FLAG_INTEGRITY = 0x02;
    public static final int FLAG_CLASS_SOURCE = 0x04;

    private HandshakeChallenge() { }

    /**
     * Build the handshake challenge response hex digest.
     *
     * @param version       CatAntiCheat protocol version (from SpoofConfig.protocolVersion())
     * @param salt          salt byte from {@code SPacketHello}
     * @param nonce         challenge nonce (int) from {@code SPacketHello}
     * @param flags         challenge flags bitmask from {@code SPacketHello}
     * @param integrityHex  integrity fingerprint (ld) from {@link FingerprintUtils#getClientIntegrityFingerprint()}
     * @param classSourceHex class-source fingerprint (le) from {@link FingerprintUtils#getClientClassSourceFingerprint()}
     * @return 40-char uppercase hex SHA-1 digest, or "ERROR" on failure
     */
    public static String buildResponse(int version, byte salt, int nonce, int flags,
                                        String integrityHex, String classSourceHex) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            appendField(md, "nonce", String.valueOf(nonce));
            appendField(md, "salt",  String.valueOf(salt & 0xFF));
            appendField(md, "flags", String.valueOf(flags));
            if ((flags & FLAG_VERSION) != 0) {
                appendField(md, "version", String.valueOf(version));
            }
            if ((flags & FLAG_INTEGRITY) != 0) {
                appendField(md, "integrity", integrityHex);
            }
            if ((flags & FLAG_CLASS_SOURCE) != 0) {
                appendField(md, "classSource", classSourceHex);
            }
            byte[] digest = md.digest();
            return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest)).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            return "ERROR";
        }
    }

    private static void appendField(MessageDigest md, String key, String value) {
        md.update(key.getBytes(StandardCharsets.UTF_8));
        md.update('\0');
        md.update((value != null ? value : "").getBytes(StandardCharsets.UTF_8));
        // The original helper ends every field with MessageDigest.update((byte)-1).
        md.update((byte) 0xFF);
    }
}
