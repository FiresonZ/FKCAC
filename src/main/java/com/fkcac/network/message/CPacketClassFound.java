package com.fkcac.network.message;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

import java.util.Collections;
import java.util.List;

/**
 * C -> S, discriminator 6, "CatAntiCheat" channel.
 * Reply to {@link SPacketClassCheck}: the list of queried classes actually found,
 * followed by the salt.
 */
public class CPacketClassFound implements IMessage {
    private final List<String> foundClassList;
    private final byte salt;

    public CPacketClassFound() {
        this(Collections.<String>emptyList(), (byte) 0);
    }

    public CPacketClassFound(List<String> foundClassList, byte salt) {
        this.foundClassList = foundClassList;
        this.salt = salt;
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
        buf.writeByte(salt);
    }
}