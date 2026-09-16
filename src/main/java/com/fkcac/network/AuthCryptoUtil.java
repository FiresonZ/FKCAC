package com.fkcac.network;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Mirrors {@code luohuayu.anticheat.auth.AuthCryptoUtil} from the server's CatAntiCheat jar.
 *
 * <p>{@code buildResponse(clientId, clientSalt, challenge)} computes:
 * <pre>
 *   SHA-1("response\0" + challenge + '\0' + clientId + '\0' + clientSalt)
 * </pre>
 * and returns the result as a 40-character uppercase hex string.
 */
public final class AuthCryptoUtil {
    private AuthCryptoUtil() { }

    /**
     * Build the auth challenge-response digest.
     *
     * @param clientId    client identity (e.g. {@code "catanticheat-client"})
     * @param clientSalt  hardcoded client salt (e.g. {@code "NiuNiu-CAC-CLI-2026-H7p4Ds9Jx2Qm8Lv5Rk1T"})
     * @param challenge   the challenge string issued by the server
     * @return 40-char uppercase hex SHA-1 digest, or "ERROR" on failure
     */
    public static String buildResponse(String clientId, String clientSalt, String challenge) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            md.update("response".getBytes(StandardCharsets.UTF_8));
            md.update('\0');
            md.update(challenge.getBytes(StandardCharsets.UTF_8));
            md.update('\0');
            md.update(clientId.getBytes(StandardCharsets.UTF_8));
            md.update('\0');
            md.update(clientSalt.getBytes(StandardCharsets.UTF_8));
            byte[] digest = md.digest();
            return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest)).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            return "ERROR";
        }
    }
}
