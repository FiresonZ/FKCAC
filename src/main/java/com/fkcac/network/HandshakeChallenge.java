package com.fkcac.network;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Mirrors {@code luohuayu.anticheat.HandshakeChallenge} from the server's CatAntiCheat jar.
 *
 * <p>Computes a SHA-1 hex digest over a structured challenge payload:
 * <pre>
 *   "nonce" + str(nonce) + '\0' +
 *   "salt"  + str(salt & 0xFF) + '\0' +
 *   "flags" + str(flags) + '\0' +
 *   [if flags & 0x01]  "version" + str(version) + '\0'
 *   [if flags & 0x02]  "integrity" + integrityHex + '\0'
 *   [if flags & 0x04]  "classSource" + classSourceHex + '\0'
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
     * @param integrityHex  integrity fingerprint (ld) from {@link ProtocolUtils#getClientIntegrityFingerprint()}
     * @param classSourceHex class-source fingerprint (le) from {@link ProtocolUtils#getClientClassSourceFingerprint()}
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
        md.update(value.getBytes(StandardCharsets.UTF_8));
    }
}
