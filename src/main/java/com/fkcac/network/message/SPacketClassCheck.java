package com.fkcac.network.message;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

import java.util.ArrayList;
import java.util.List;

/**
 * S -> C, discriminator 2, "CatAntiCheat" channel.
 * Server asks which of the given class names are present on the client.
 * The client must reply with {@link CPacketClassFound}.
 */
public class SPacketClassCheck implements IMessage {
    private final List<String> classList = new ArrayList<String>();

    public SPacketClassCheck() { }

    @Override
    public void fromBytes(ByteBuf buf) {
        int size = buf.readShort();
        for (int i = 0; i < size; i++) {
            classList.add(ByteBufUtils.readUTF8String(buf));
        }
    }

    @Override
    public void toBytes(ByteBuf buf) { // server -> client only
    }

    public List<String> getClassList() {
        return classList;
    }
}