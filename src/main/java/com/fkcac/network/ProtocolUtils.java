package com.fkcac.network;

import net.minecraft.launchwrapper.LaunchClassLoader;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;
import java.net.UnknownServiceException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Protocol-faithful helpers that mirror {@code luohuayu.anticheat.CheckUtils} from
 * CatAntiCheat-Public, so the replies FKCAC fabricates are indistinguishable from a
 * healthy client:
 *
 * <ul>
 *   <li>{@link #checkClass(List)} - reports exactly the queried classes that are
 *       actually loadable (the server always includes a "marker" class it expects to
 *       find, so it must be reported as present).</li>
 *   <li>{@link #checkFile(File)} - reports the real {@code SHA1\0filename} hashes of
 *       every launch source except FKCAC's own jar, keeping the count above the
 *       server's required minimum of 5 distinct entries.</li>
 * </ul>
 */
public final class ProtocolUtils {
    private ProtocolUtils() { }

    /**
     * Report which of the queried class names are actually loadable. Mirrors the real
     * client's {@code CheckUtils.checkClass}.
     */
    public static List<String> checkClass(List<String> classList) {
        List<String> foundClass = new ArrayList<String>();
        if (classList == null) {
            return foundClass;
        }
        for (String className : classList) {
            try {
                Class.forName(className);
                foundClass.add(className);
            } catch (ClassNotFoundException e) {
                try {
                    Class.forName(className, true, ClassLoader.getSystemClassLoader());
                    foundClass.add(className);
                } catch (ClassNotFoundException ignored) { }
            }
        }
        return foundClass;
    }

    /**
     * Compute the real launch-source hashes in the exact format the server expects
     * ({@code SHA1-uppercase \0 filename}), excluding FKCAC's own jar so the reported
     * mod set stays inside the server's allow-list.
     */
    public static List<String> checkFile(File selfJar) {
        Set<String> fileHash = new HashSet<String>();
        try {
            LaunchClassLoader lwClassloader = (LaunchClassLoader) ProtocolUtils.class.getClassLoader();
            for (URL source : lwClassloader.getSources()) {
                if (selfJar != null && isSelfSource(source, selfJar)) {
                    continue;
                }
                String hash = getFileHash(source);
                if (hash != null) {
                    fileHash.add(hash);
                }
            }
        } catch (ClassCastException ignored) {
            // Not running under the launch wrapper (e.g. unit test) - nothing to scan.
        }
        return new ArrayList<String>(fileHash);
    }

    private static boolean isSelfSource(URL source, File selfJar) {
        try {
            return new File(source.getFile()).getName().equals(selfJar.getName());
        } catch (Exception e) {
            return false;
        }
    }

    private static String getFileHash(URL url) {
        String fileName = new File(url.getFile()).getName();
        try {
            InputStream in = url.openStream();
            try {
                return calcHash(in) + "\0" + fileName;
            } finally {
                try {
                    in.close();
                } catch (IOException ignored) { }
            }
        } catch (UnknownServiceException e) {
            return null;
        } catch (IOException e) {
            // Mirror the real client: unreadable files are reported as all-zero hashes.
            return "0000000000000000000000000000000000000000\0" + (fileName.isEmpty() ? "unknown" : fileName);
        }
    }

    private static String calcHash(InputStream in) throws IOException {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA1");
            final byte[] buffer = new byte[4096];
            int read;
            while ((read = in.read(buffer, 0, 4096)) > -1) {
                md.update(buffer, 0, read);
            }
            byte[] digest = md.digest();
            return String.format("%0" + (digest.length << 1) + "x", new BigInteger(1, digest)).toUpperCase();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}