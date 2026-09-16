package com.fkcac.network.message;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/**
 * C -> S, discriminator 4, "CatAntiCheat" channel.
 * Reply to {@link SPacketHello}: reports the client protocol version, echoes the salt,
 * and provides three fingerprint strings:
 * <ul>
 *   <li>{@code ld} — integrity fingerprint (SHA-1 of class resource bytes from the JAR)</li>
 *   <li>{@code le} — class-source fingerprint (SHA-1 of class name paths)</li>
 *   <li>{@code lf} — handshake challenge response (SHA-1 of nonce+salt+flags+...)</li>
 * </ul>
 */
public class CPacketHelloReply implements IMessage {
    /** CatAntiCheat client protocol version (the real client sends 2). */
    private final int version;
    /** Salt echoed from the server's hello. */
    private final byte salt;
    /** Integrity fingerprint: SHA-1 of class file resources inside the FKCAC jar. */
    private final String ld;
    /** Class-source fingerprint: SHA-1 of canonical class name strings. */
    private final String le;
    /** Handshake challenge response: SHA-1 hex digest computed by HandshakeChallenge. */
    private final String lf;

    public CPacketHelloReply() {
        this(0, (byte) 0, "", "", "");
    }

    public CPacketHelloReply(int version, byte salt, String ld, String le, String lf) {
        this.version = version;
        this.salt = salt;
        this.ld = ld != null ? ld : "";
        this.le = le != null ? le : "";
        this.lf = lf != null ? lf : "";
    }

    @Override
    public void fromBytes(ByteBuf buf) { // client -> server only
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeShort(version);
        buf.writeByte(salt);
        ByteBufUtils.writeUTF8String(buf, ld);
        ByteBufUtils.writeUTF8String(buf, le);
        ByteBufUtils.writeUTF8String(buf, lf);
    }
}
