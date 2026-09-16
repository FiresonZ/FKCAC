package com.fkcac.network;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Mirrors {@code luohuayu.anticheat.CheckUtils} from the server's CatAntiCheat jar.
 *
 * <p>Two fingerprint algorithms are implemented:
 * <ul>
 *   <li>{@link #getClientIntegrityFingerprint()} — hashes the raw bytes of specific class
 *       resources found inside this mod's own JAR, producing the {@code ld} field.</li>
 *   <li>{@link #getClientClassSourceFingerprint()} — hashes the canonical name strings
 *       (converted to slash-separated paths) for the same set of classes, producing the
 *       {@code le} field.</li>
 * </ul>
 *
 * <p>These fingerprints are included in {@code CPacketHelloReply} so the server can
 * verify that the client is running the expected mod build.
 */
public final class FingerprintUtils {

    /** Resource paths inside the FKCAC JAR whose raw bytes are hashed for the integrity fingerprint. */
    private static final List<String> INTEGRITY_PATHS = Collections.unmodifiableList(Arrays.asList(
            "/META-INF/MANIFEST.MF",
            "/com/fkcac/FKCAC.class",
            "/com/fkcac/network/HandshakeChallenge.class",
            "/com/fkcac/network/FKCACProtocolHandler.class",
            "/com/fkcac/network/message/SPacketHello.class",
            "/com/fkcac/network/message/CPacketHelloReply.class"
    ));

    /** Canonical class names whose slash-separated paths are hashed for the class-source fingerprint. */
    private static final List<String> CLASS_SOURCE_NAMES = Collections.unmodifiableList(Arrays.asList(
            "com.fkcac.FKCAC",
            "com.fkcac.network.HandshakeChallenge",
            "com.fkcac.network.FKCACProtocolHandler",
            "com.fkcac.network.message.SPacketHello",
            "com.fkcac.network.message.CPacketHelloReply"
    ));

    private FingerprintUtils() { }

    /**
     * Compute the integrity fingerprint (ld) by hashing each listed resource's raw bytes.
     * If a resource is not found its bytes contribute the literal {@code "missing"}.
     *
     * @return 40-char uppercase hex SHA-1 digest, or "ERROR" on failure
     */
    public static String getClientIntegrityFingerprint() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            ClassLoader cl = FingerprintUtils.class.getClassLoader();
            for (String path : INTEGRITY_PATHS) {
                md.update(path.getBytes(StandardCharsets.UTF_8));
                InputStream is = cl.getResourceAsStream(path);
                if (is == null) {
                    md.update("missing".getBytes(StandardCharsets.UTF_8));
                } else {
                    try {
                        byte[] bytes = readAll(is);
                        md.update(bytes);
                    } finally {
                        try { is.close(); } catch (IOException ignored) { }
                    }
                }
            }
            byte[] digest = md.digest();
            return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest)).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            return "ERROR";
        }
    }

    /**
     * Compute the class-source fingerprint (le) by hashing each class name after
     * converting dots to slashes and prepending a leading slash.
     *
     * @return 40-char uppercase hex SHA-1 digest, or "ERROR" on failure
     */
    public static String getClientClassSourceFingerprint() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            for (String name : CLASS_SOURCE_NAMES) {
                md.update(name.getBytes(StandardCharsets.UTF_8));
                md.update(normalizePath(name).getBytes(StandardCharsets.UTF_8));
            }
            byte[] digest = md.digest();
            return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest)).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            return "ERROR";
        }
    }

    /** Convert {@code com.example.Foo} → {@code /com/example/Foo}. */
    private static String normalizePath(String className) {
        return "/" + className.replace('.', '/');
    }

    private static byte[] readAll(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[4096];
        int n;
        while ((n = in.read(buf)) != -1) {
            out.write(buf, 0, n);
        }
        return out.toByteArray();
    }
}
