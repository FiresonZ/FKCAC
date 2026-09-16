package com.fkcac.network;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.UnknownServiceException;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Mirrors {@code luohuayu.anticheat.CheckUtils} from the server's modified CatAntiCheat jar,
 * producing the {@code ld} / {@code le} fingerprints of {@code CPacketHelloReply}.
 *
 * <p>{@link #getClientIntegrityFingerprint()} (ld): for every entry in the original
 * {@code ky} path list, updates the digest with the path bytes, then with either the
 * SHA-1 of the resource bytes (when the resource exists) or the literal {@code "missing"}.
 *
 * <p>{@link #getClientClassSourceFingerprint()} (le): for every entry in the original
 * {@code kz} class list, updates the digest with the canonical name bytes and the
 * code-source locator of that class ({@code x(name)} → {@code "hash:&lt;jar-sha1&gt;\0&lt;file&gt;"},
 * {@code "file:..."}, {@code "url:..."}, {@code "loader:..."} or {@code "error:&lt;name&gt;"}
 * if the class cannot be loaded).
 *
 * <p>The path/class lists are kept identical to the reference client. Under FKCAC the
 * {@code luohuayu.*} resources/classes do not exist, so the same deterministic formulas
 * produce {@code missing} / {@code error:...} contributions; the handshake response
 * {@code lf} is therefore still bound to exactly the same algorithm the server expects.
 */
public final class FingerprintUtils {

    /** Original {@code ky}: resource paths inside the anti-cheat jar whose content participates in ld. */
    private static final List<String> INTEGRITY_PATHS = Collections.unmodifiableList(Arrays.asList(
            "/META-INF/MANIFEST.MF",
            "/luohuayu/anticheat/CatAntiCheatMod.class",
            "/luohuayu/anticheat/AntiCheatPacketMessageHandler.class",
            "/luohuayu/anticheat/RuntimeInjectCheck.class",
            "/luohuayu/anticheat/asm/AntiCheatCorePlugin.class",
            "/luohuayu/anticheat/asm/AntiCheatTransformer.class"
    ));

    /** Original {@code kz}: canonical class names whose code-source location participates in le. */
    private static final List<String> CLASS_SOURCE_NAMES = Collections.unmodifiableList(Arrays.asList(
            "luohuayu.anticheat.CatAntiCheatMod",
            "luohuayu.anticheat.AntiCheatPacketMessageHandler",
            "luohuayu.anticheat.RuntimeInjectCheck",
            "luohuayu.anticheat.asm.AntiCheatCorePlugin",
            "luohuayu.anticheat.asm.AntiCheatTransformer"
    ));

    private FingerprintUtils() { }

    /**
     * Integrity fingerprint (ld): {@code SHA-1(path + SHA1(resource) | "missing")} over the
     * original path list. 40-char uppercase hex, or "ERROR".
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
                        md.update(sha1(readAll(is)));
                    } finally {
                        try { is.close(); } catch (IOException ignored) { }
                    }
                }
            }
            return toHex(md.digest());
        } catch (Exception e) {
            return "ERROR";
        }
    }

    /**
     * Class-source fingerprint (le): {@code SHA-1(name + x(name))} over the original class
     * list, where {@code x(name)} is the code-source locator of that class.
     */
    public static String getClientClassSourceFingerprint() {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            for (String name : CLASS_SOURCE_NAMES) {
                md.update(name.getBytes(StandardCharsets.UTF_8));
                md.update(sourceOf(name).getBytes(StandardCharsets.UTF_8));
            }
            return toHex(md.digest());
        } catch (Exception e) {
            return "ERROR";
        }
    }

    /** Original {@code x(String)}: code-source locator of a loaded class. */
    private static String sourceOf(String className) {
        try {
            Class<?> cls = Class.forName(className, false, FingerprintUtils.class.getClassLoader());
            ProtectionDomain pd = cls.getProtectionDomain();
            CodeSource cs = pd != null ? pd.getCodeSource() : null;
            URL location = cs != null ? cs.getLocation() : null;
            if (location != null) {
                return describeUrl(location);
            }
            ClassLoader cl = cls.getClassLoader();
            return cl != null ? "loader:" + cl.getClass().getName() : "loader:bootstrap";
        } catch (Exception e) {
            return "error:" + className;
        }
    }

    /** Original {@code b(URL)}: human-readable form of a class's code-source URL. */
    private static String describeUrl(URL url) {
        if (url == null) {
            return "source:unknown";
        }
        String hash = fileHash(url);
        if (hash != null && !hash.isEmpty()) {
            return "hash:" + hash;
        }
        String fileName = new File(url.getFile()).getName();
        if (!fileName.isEmpty()) {
            return "file:" + fileName;
        }
        return "url:" + url.getProtocol();
    }

    /** Original {@code a(URL)}: {@code <uppercase-SHA1 of stream>\0<file name>}, or null / zeros. */
    private static String fileHash(URL url) {
        String fileName = new File(url.getFile()).getName();
        try {
            InputStream in = url.openStream();
            try {
                return sha1Hex(in) + "\0" + fileName;
            } finally {
                try { in.close(); } catch (IOException ignored) { }
            }
        } catch (UnknownServiceException e) {
            return null;
        } catch (IOException e) {
            return "0000000000000000000000000000000000000000\0"
                    + (fileName.isEmpty() ? "unknown" : fileName);
        }
    }

    private static byte[] sha1(InputStream in) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer, 0, 4096)) > -1) {
                md.update(buffer, 0, read);
            }
            return md.digest();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private static String sha1Hex(InputStream in) throws IOException {
        return toHex(sha1(in));
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

    private static String toHex(byte[] digest) {
        return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest)).toUpperCase();
    }
}