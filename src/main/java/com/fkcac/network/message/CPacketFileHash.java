package com.fkcac.network.message;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPOutputStream;

/**
 * C -> S, discriminator 5, "CatAntiCheat" channel.
 * Reply to {@link SPacketFileCheck}: a GZIP-compressed, newline separated list of
 * file hashes ("file=<md5>..."), prefixed with the salt, terminated by a 2-byte length
 * and an integer hash of the gzip data.
 */
public class CPacketFileHash implements IMessage {
    private final List<String> fileHashList;
    private final byte salt;

    public CPacketFileHash() {
        this(java.util.Collections.<String>emptyList(), (byte) 0);
    }

    public CPacketFileHash(List<String> fileHashList, byte salt) {
        this.fileHashList = fileHashList;
        this.salt = salt;
    }

    @Override
    public void fromBytes(ByteBuf buf) { // client -> server only
    }

    @Override
    public void toBytes(ByteBuf buf) {
        if (fileHashList == null || fileHashList.size() == 0) {
            throw new RuntimeException("Hash is empty");
        }

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            GZIPOutputStream gzipOutputStream = new GZIPOutputStream(out);
            gzipOutputStream.write(salt);

            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(gzipOutputStream, StandardCharsets.UTF_8));
            for (int i = 0; i < fileHashList.size(); i++) {
                if (i > 0) {
                    writer.newLine();
                }
                writer.write(fileHashList.get(i));
            }
            writer.flush();
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        byte[] gzipData = out.toByteArray();

        buf.writeShort(gzipData.length);
        buf.writeBytes(gzipData);
        buf.writeInt(Arrays.hashCode(gzipData));
    }
}