package com.fkcac.network.message;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

import java.util.List;

/**
 * C -> S, discriminator 7, "CatAntiCheat" channel.
 * Reports classes that look like they were injected (ASM/runtime) to the server.
 * Sent proactively on join and during {@link SPacketDataCheck}.
 */
public class CPacketInjectDetect implements IMessage {
    private final List<String> foundClassList;

    public CPacketInjectDetect() {
        this(java.util.Collections.<String>emptyList());
    }

    public CPacketInjectDetect(List<String> foundClassList) {
        this.foundClassList = foundClassList;
    }

    @Override
    public void fromBytes(ByteBuf buf) { // client -> server only
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeShort(foundClassList.size());
        for (String s : foundClassList) {
            ByteBufUtils.writeUTF8String(buf, s);
        }
    }
}