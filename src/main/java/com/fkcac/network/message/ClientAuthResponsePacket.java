package com.fkcac.network.message;

import com.fkcac.network.NetUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/**
 * C -> S, discriminator 13, "CatAntiCheat" channel.
 * Response to {@link ServerAuthChallengePacket}: carries the client id, client salt,
 * and the SHA-1 response digest. All three strings use the VarInt codec
 * (see {@link NetUtils}), matching the reference packet.
 */
public class ClientAuthResponsePacket implements IMessage {
    private final String clientId;
    private final String clientSalt;
    private final String response;

    public ClientAuthResponsePacket() {
        this("", "", "");
    }

    public ClientAuthResponsePacket(String clientId, String clientSalt, String response) {
        this.clientId = clientId != null ? clientId : "";
        this.clientSalt = clientSalt != null ? clientSalt : "";
        this.response = response != null ? response : "";
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetUtils.writeString(buf, clientId);
        NetUtils.writeString(buf, clientSalt);
        NetUtils.writeString(buf, response);
    }
}
