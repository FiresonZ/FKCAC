package com.fkcac.network.message;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/**
 * C -> S, discriminator 4, "CatAntiCheat" channel.
 * Reply to {@link SPacketHello}: reports the client protocol version and echoes the salt.
 */
public class CPacketHelloReply implements IMessage {
    /** CatAntiCheat client protocol version (the real client sends 2). */
    private final int version;
    /** Salt echoed from the server's hello. */
    private final byte salt;

    public CPacketHelloReply() {
        this(0, (byte) 0);
    }

    public CPacketHelloReply(int version, byte salt) {
        this.version = version;
        this.salt = salt;
    }

    @Override
    public void fromBytes(ByteBuf buf) { // client -> server only
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeShort(version);
        buf.writeByte(salt);
    }
}