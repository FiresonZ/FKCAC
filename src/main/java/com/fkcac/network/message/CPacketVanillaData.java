package com.fkcac.network.message;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/**
 * C -> S, discriminator 10, "CatAntiCheat" channel.
 * Reply to {@link SPacketDataCheck}. Wire format matches the reference:
 * <pre>
 *   boolean  lighting       = gamma &gt; 1.5 (brightness boost)
 *   boolean  transparent    = suspicious vip texture pack detected
 *   UTF8     description    = name/description of the suspicious texture ("" when clean)
 *   UTF8     response       = SHA-1 response binding nonce + rules fingerprint
 * </pre>
 * The last two strings are MANDATORY: the server reads them unconditionally.
 */
public class CPacketVanillaData implements IMessage {
    private final boolean lighting;
    private final boolean transparent;
    private final String description;
    private final String response;

    public CPacketVanillaData() {
        this(false, false, "", "");
    }

    public CPacketVanillaData(boolean lighting, boolean transparent, String description, String response) {
        this.lighting = lighting;
        this.transparent = transparent;
        this.description = description != null ? description : "";
        this.response = response != null ? response : "";
    }

    @Override
    public void fromBytes(ByteBuf buf) { // client -> server only
    }

    @Override
    public void toBytes(ByteBuf buf) {
        buf.writeBoolean(lighting);
        buf.writeBoolean(transparent);
        ByteBufUtils.writeUTF8String(buf, description);
        ByteBufUtils.writeUTF8String(buf, response);
    }
}