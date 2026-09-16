package com.fkcac.network.message;

import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/**
 * S -> C, discriminator 1, "CatAntiCheat" channel.
 * Server requests a file/MD5 list. The client must respond with {@link CPacketFileHash}.
 */
public class SPacketFileCheck implements IMessage {

    public SPacketFileCheck() { }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) { // server -> client only
    }
}