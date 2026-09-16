package com.fkcac.network.message;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/**
 * S -> C, discriminator 0, "CatAntiCheat" channel.
 * Server sends a random salt to start the handshake; the client must reply with
 * {@link CPacketHelloReply} echoing the salt together with its protocol version.
 */
public class SPacketHello implements IMessage {
    /** Random salt issued by the server, must be echoed back. */
    public byte salt;

    public SPacketHello() { }

    @Override
    public void fromBytes(ByteBuf buf) {
        salt = buf.readByte();
    }

    @Override
    public void toBytes(ByteBuf buf) { // server -> client only
    }
}