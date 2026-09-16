package com.fkcac.network.message;

import com.fkcac.network.NetUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/**
 * C -> S, discriminator 15, "CatAntiCheat" channel.
 * Security/environment profile the real client reports right after the handshake.
 * Wire format (matches the reference): 9 VarInt-UTF8 strings, then a {@code long}
 * timestamp, then 1 final VarInt-UTF8 string (launcher version).
 * Fields: coreA/B/C (strings), envFingerprint, source fields, sessionId, timestamp, launcherVersion.
 */
public class CPacketSecurityProfile implements IMessage {
    private String coreA;
    private String coreB;
    private String coreC;
    private String envFingerprint;
    private String coreASource;
    private String coreBSource;
    private String coreCSource;
    private String envSource;
    private String clientSessionId;
    private long reportedAt;
    private String launcherVersion;

    public CPacketSecurityProfile() { }

    public CPacketSecurityProfile(String coreA, String coreB, String coreC,
                                   String envFingerprint,
                                   String coreASource, String coreBSource, String coreCSource,
                                   String envSource, String clientSessionId,
                                   long reportedAt, String launcherVersion) {
        this.coreA = coreA != null ? coreA : "";
        this.coreB = coreB != null ? coreB : "";
        this.coreC = coreC != null ? coreC : "";
        this.envFingerprint = envFingerprint != null ? envFingerprint : "";
        this.coreASource = coreASource != null ? coreASource : "";
        this.coreBSource = coreBSource != null ? coreBSource : "";
        this.coreCSource = coreCSource != null ? coreCSource : "";
        this.envSource = envSource != null ? envSource : "";
        this.clientSessionId = clientSessionId != null ? clientSessionId : "";
        this.reportedAt = reportedAt;
        this.launcherVersion = launcherVersion != null ? launcherVersion : "";
    }

    @Override
    public void fromBytes(ByteBuf buf) {
    }

    @Override
    public void toBytes(ByteBuf buf) {
        NetUtils.writeString(buf, coreA);
        NetUtils.writeString(buf, coreB);
        NetUtils.writeString(buf, coreC);
        NetUtils.writeString(buf, envFingerprint);
        NetUtils.writeString(buf, coreASource);
        NetUtils.writeString(buf, coreBSource);
        NetUtils.writeString(buf, coreCSource);
        NetUtils.writeString(buf, envSource);
        NetUtils.writeString(buf, clientSessionId);
        buf.writeLong(reportedAt);
        NetUtils.writeString(buf, launcherVersion);
    }
}
