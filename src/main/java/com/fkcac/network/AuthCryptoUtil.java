package com.fkcac.network;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Mirrors {@code luohuayu.anticheat.auth.AuthCryptoUtil} from the server's modified
 * CatAntiCheat jar.
 *
 * <p>{@code buildResponse(clientId, challenge, clientSalt)} computes:
 * <pre>
 *   SHA-1(
 *     "response" + 0xFF +
 *     clientId   + 0xFF +
 *     challenge  + 0xFF +
 *     clientSalt + 0xFF)
 * </pre>
 * Each part is appended as {@code value + (byte)0xFF} — the reference helper calls
 * {@code MessageDigest.update(valueBytes)} then {@code MessageDigest.update((byte)-1)}.
 * The argument order matches the reference call site:
 * {@code AuthCryptoUtil.buildResponse(AuthController.buildClientId(), challenge, CLIENT_SALT)}.
 * The result is a 40-character uppercase hex string.
 */
public final class AuthCryptoUtil {
    private AuthCryptoUtil() { }

    /**
     * Build the auth challenge-response digest.
     *
     * @param clientId    client identity (e.g. {@code "catanticheat-client"})
     * @param challenge   the challenge string issued by the server
     * @param clientSalt  hardcoded client salt (e.g. {@code "NiuNiu-CAC-CLI-2026-H7p4Ds9Jx2Qm8Lv5Rk1T"})
     * @return 40-char uppercase hex SHA-1 digest, or "ERROR" on failure
     */
    public static String buildResponse(String clientId, String challenge, String clientSalt) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            append(md, "response");
            append(md, clientId);
            append(md, challenge);
            append(md, clientSalt);
            return toHex(md.digest());
        } catch (NoSuchAlgorithmException e) {
            return "ERROR";
        }
    }

    private static void append(MessageDigest md, String value) {
        md.update((value != null ? value : "").getBytes(StandardCharsets.UTF_8));
        md.update((byte) 0xFF);
    }

    private static String toHex(byte[] digest) {
        StringBuilder sb = new StringBuilder(digest.length * 2);
        for (byte b : digest) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }
}