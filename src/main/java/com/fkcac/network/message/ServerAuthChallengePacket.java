package com.fkcac.network.message;

import com.fkcac.network.NetUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/**
 * S -> C, discriminator 12, "CatAntiCheat" channel.
 * Server challenges the client with a random base64-like string.
 * The challenge is read as {@code VarInt length + UTF-8 bytes} (see {@link NetUtils}).
 */
public class ServerAuthChallengePacket implements IMessage {
    /** The challenge string issued by the server. */
    public String challenge;

    public ServerAuthChallengePacket() { }

    @Override
    public void fromBytes(ByteBuf buf) {
        challenge = NetUtils.readString(buf);
    }

    @Override
    public void toBytes(ByteBuf buf) {
    }
}
