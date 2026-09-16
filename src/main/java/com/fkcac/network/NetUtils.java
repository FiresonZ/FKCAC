package com.fkcac.network;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

/**
 * Mirrors {@code luohuayu.anticheat.a} ("NetUtils") from the server's modified
 * CatAntiCheat jar.
 *
 * <p>The auth packets and {@code CPacketSecurityProfile} do NOT use FML's
 * {@code ByteBufUtils} (2-byte short length prefix). They use a custom codec:
 * <pre>
 *   writeString(buf, s): VarInt(len(utf8 bytes)) + utf8 bytes
 *   readString(buf)    : VarInt(max 2 bytes on read) + utf8 slice
 * </pre>
 * The reader is bounded to 2 VarInt bytes and throws {@code "VarInt too big"}
 * beyond that, exactly like the reference.
 */
public final class NetUtils {
    private NetUtils() { }

    /** Read a VarInt; throws if it exceeds {@code maxBytes} variable bytes. */
    public static int readVarInt(ByteBuf buf, int maxBytes) {
        int value = 0;
        int count = 0;
        while (true) {
            byte b = buf.readByte();
            value |= (b & 0x7F) << (7 * count);
            count++;
            if (count > maxBytes) {
                throw new RuntimeException("VarInt too big");
            }
            if ((b & 0x80) != 0x80) {
                return value;
            }
        }
    }

    /** Write a standard 7-bit VarInt. */
    public static void writeVarInt(ByteBuf buf, int value) {
        while ((value & -128) != 0) {
            buf.writeByte((value & 0x7F) | 0x80);
            value >>>= 7;
        }
        buf.writeByte(value);
    }

    /** Read a VarInt-prefixed UTF-8 string (matches the reference reader, max 2 bytes). */
    public static String readString(ByteBuf buf) {
        int len = readVarInt(buf, 2);
        int idx = buf.readerIndex();
        String s = buf.toString(idx, len, StandardCharsets.UTF_8);
        buf.readerIndex(idx + len);
        return s;
    }

    /** Write a VarInt-prefixed UTF-8 string; null is written as the empty string. */
    public static void writeString(ByteBuf buf, String s) {
        if (s == null) {
            s = "";
        }
        byte[] bytes = s.getBytes(StandardCharsets.UTF_8);
        writeVarInt(buf, bytes.length);
        buf.writeBytes(bytes);
    }
}