package com.fkcac.network.message;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/**
 * S -> C, discriminator 12, "CatAntiCheat" channel.
 * Server challenges the client with a random base64-like string.
 */
public class ServerAuthChallengePacket implements IMessage {
    /** The challenge string issued by the server. */
    public String challenge;

    public ServerAuthChallengePacket() { }

    @Override
    public void fromBytes(ByteBuf buf) {
        challenge = ByteBufUtils.readUTF8String(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }
}
