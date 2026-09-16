package com.fkcac.network.message;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/**
 * S -> C, discriminator 0, "CatAntiCheat" channel.
 * Server sends a random salt, a challenge nonce, and a flags bitmask to start the handshake.
 * The client must reply with {@link CPacketHelloReply} echoing the salt and providing
 * three fingerprint strings (integrity, class-source, handshake-response).
 */
public class SPacketHello implements IMessage {
    /** Random salt issued by the server, must be echoed back. */
    public byte salt;
    /** Random nonce used in the handshake challenge response. */
    public int challengeNonce;
    /** Bitmask of challenge flags (FLAG_VERSION=1, FLAG_INTEGRITY=2, FLAG_CLASS_SOURCE=4). */
    public int challengeFlags;

    public SPacketHello() { }

    @Override
    public void fromBytes(ByteBuf buf) {
        salt = buf.readByte();
        challengeNonce = buf.readInt();
        challengeFlags = buf.readByte() & 0xFF;
    }

    @Override
    public void toBytes(ByteBuf buf) {
        // server -> client only
    }
}
