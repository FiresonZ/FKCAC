package com.fkcac.network.message;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;

/**
 * C -> S, discriminator 15, "CatAntiCheat" channel.
 * Server-initiated security profile request. FKCAC answers with an empty profile.
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
        ByteBufUtils.writeUTF8String(buf, coreA);
        ByteBufUtils.writeUTF8String(buf, coreB);
        ByteBufUtils.writeUTF8String(buf, coreC);
        ByteBufUtils.writeUTF8String(buf, envFingerprint);
        ByteBufUtils.writeUTF8String(buf, coreASource);
        ByteBufUtils.writeUTF8String(buf, coreBSource);
        ByteBufUtils.writeUTF8String(buf, coreCSource);
        ByteBufUtils.writeUTF8String(buf, envSource);
        ByteBufUtils.writeUTF8String(buf, clientSessionId);
        buf.writeLong(reportedAt);
        ByteBufUtils.writeUTF8String(buf, launcherVersion);
    }
}
