# Server-Side CatAntiCheat Reverse Engineering Analysis

Decompiled from `CatAntiCheat1710-release.jar.disabled` (server version, modified vs. open-source).
Analysis date: 2026-09-16.

---

## 1. Protocol Overview

The server uses the FML plugin channel `"CatAntiCheat"` with discriminators 0–15:

| # | Direction | Class | Name | Status |
|---|-----------|-------|------|--------|
| 0 | S→C | `SPacketHello` | Handshake challenge | Implemented in this mod |
| 1 | S→C | `SPacketFileCheck` | File hash query | Implemented |
| 2 | S→C | `SPacketClassCheck` | Class existence query | Implemented |
| 3 | S→C | `SPacketScreenshot` | Screenshot request | Implemented |
| 4 | C→S | `CPacketHelloReply` | Handshake reply | **Need patch: add 3 fingerprint strings** |
| 5 | C→S | `CPacketFileHash` | File hash response | Implemented |
| 6 | C→S | `CPacketClassFound` | Class found response | Implemented |
| 7 | C→S | `CPacketInjectDetect` | Runtime inject detection | New (empty handler) |
| 8 | C→S | `CPacketImageData` | Screenshot image data | Implemented |
| 9 | S→C | `SPacketDataCheck` | Data check challenge | Implemented |
| 10 | C→S | `CPacketVanillaData` | Vanilla data response | Implemented |
| 11 | C→S | `ClientAuthHelloPacket` | Auth session start | New |
| 12 | S→C | `ServerAuthChallengePacket` | Auth challenge | New |
| 13 | C→S | `ClientAuthResponsePacket` | Auth response | New |
| 14 | S→C | `ServerAuthResultPacket` | Auth result | New |
| 15 | C→S | `CPacketSecurityProfile` | System info report | New (empty handler) |

---

## 2. SPacketHello (Discriminator 0)

```java
// Wire format (big-endian):
//   byte  salt               (1 byte, random)
//   int   challengeNonce     (4 bytes, random)
//   byte  challengeFlags     (1 byte, bitmask, zero-extended to int)
//
// Flags:
//   FLAG_VERSION    = 0x01  → include protocol version in handshake hash
//   FLAG_INTEGRITY  = 0x02  → include integrity fingerprint in handshake hash
//   FLAG_CLASS_SOURCE = 0x04 → include class-source fingerprint in handshake hash
public class SPacketHello implements IMessage {
    public byte salt;
    public int challengeNonce;
    public int challengeFlags;  // actually a byte read from wire, masked to int
}
```

**Current FKCAC bug**: only reads `salt`, ignores nonce and flags.

---

## 3. CPacketHelloReply (Discriminator 4)

```java
// Wire format:
//   short  version    (2 bytes, NOT 4 — uses writeShort)
//   byte   salt       (1 byte, echoed from SPacketHello)
//   UTF8   ld         (integrity fingerprint SHA-1 hex string)
//   UTF8   le         (class-source fingerprint SHA-1 hex string)
//   UTF8   lf         (handshake challenge response — SHA-1 hex string)
public class CPacketHelloReply implements IMessage {
    private int version;
    private byte salt;
    private String ld;   // getClientIntegrityFingerprint()
    private String le;   // getClientClassSourceFingerprint()
    private String lf;   // HandshakeChallenge.buildResponse(version, salt, nonce, flags, ld, le)
}
```

**Current FKCAC bug**: only writes version (short) + salt (byte). Missing ld/le/lf entirely.

---

## 4. HandshakeChallenge Algorithm

```java
// Builds a SHA-1 hex digest:
//   input = "nonce" + str(nonce) + '\0' +
//           "salt"  + str(salt & 0xFF) + '\0' +
//           "flags" + str(flags) + '\0' +
//           [if flags & 0x01: "version" + str(version) + '\0'] +
//           [if flags & 0x02: "integrity" + integrityHex + '\0'] +
//           [if flags & 0x04: "classSource" + classSourceHex + '\0']
//
// Returns hex(SHA-1(input))
public static String buildResponse(int version, byte salt, int nonce, int flags,
                                    String integrity, String classSource);
```

---

## 5. CheckUtils Fingerprints

### 5.1 getClientIntegrityFingerprint() (ld)

Hashed over class **resource paths inside the JAR** (read as raw bytes via `getResourceAsStream`):

```
ky = [
  "/META-INF/MANIFEST.MF",
  "/luohuayu/anticheat/AntiCheatPacketMessageHandler.class",
  "/luohuayu/anticheat/CatAntiCheatMod.class",
  "/luohuayu/anticheat/RuntimeInjectCheck.class",
  "/luohuayu/anticheat/asm/AntiCheatCorePlugin.class",
  "/luohuayu/anticheat/asm/AntiCheatTransformer.class"
]
```

For each path: prepend the path string bytes (UTF-8), then append the raw class bytes from the resource.
If resource not found, append `"missing".getBytes(UTF-8)` instead.
Final SHA-1 → hex string.

### 5.2 getClientClassSourceFingerprint() (le)

Hashed over class **name strings only** (no class file content):

```
kz = [
  "luohuayu.anticheat.AntiCheatPacketMessageHandler",
  "luohuayu.anticheat.CatAntiCheatMod",
  "luohuayu.anticheat.RuntimeInjectCheck",
  "luohuayu.anticheat.asm.AntiCheatCorePlugin",
  "luohuayu.anticheat.asm.AntiCheatTransformer"
]
```

For each name: append the name string bytes, then append `x(name).getBytes(UTF-8)` where `x()` converts dot-separated paths to slash-separated and prepends `/`.
Final SHA-1 → hex string.

---

## 6. Auth Protocol (Discriminators 11–14)

### 6.1 ClientAuthHelloPacket (11, C→S)

```java
public class ClientAuthHelloPacket implements IMessage {
    private String kZ;  // clientId
    // toBytes: writeUTF8String(kZ)
}
```

Always sent with `clientId = "catanticheat-client"`.

### 6.2 ServerAuthChallengePacket (12, S→C)

```java
public class ServerAuthChallengePacket implements IMessage {
    public String challenge;
    // fromBytes: challenge = readUTF8String(buf)
}
```

Server sends a random base64-like challenge string.

### 6.3 ClientAuthResponsePacket (13, C→S)

```java
public class ClientAuthResponsePacket implements IMessage {
    private String kZ;  // clientId
    private String la;  // clientSalt
    private String lb;  // response (hex SHA-1)
    // toBytes: writeUTF8String(kZ) + writeUTF8String(la) + writeUTF8String(lb)
}
```

### 6.4 ServerAuthResultPacket (14, S→C)

```java
public class ServerAuthResultPacket implements IMessage {
    public boolean authorized;
    public String result;
    // fromBytes: authorized = readBoolean(); result = readUTF8String(buf)
}
```

### 6.5 Auth Crypto Algorithm

```java
// AuthController.buildResponse():
//   challenge = AuthState.challenge (from ServerAuthChallengePacket)
//   clientId  = "catanticheat-client"
//   clientSalt = "NiuNiu-CAC-CLI-2026-H7p4Ds9Jx2Qm8Lv5Rk1T"  ← HARDCODED
//
// AuthCryptoUtil.buildResponse(clientId, clientSalt, challenge):
//   input = "response\0" + challenge + '\0' + clientId + '\0' + clientSalt
//   return hex(SHA-1(input))
```

---

## 7. CPacketSecurityProfile (Discriminator 15, C→S)

```java
public class CPacketSecurityProfile implements IMessage {
    // Fields (all UTF-8 strings except ls which is long):
    //   lj  coreA
    //   lk  coreB
    //   ll  coreC
    //   lm  envFingerprint
    //   ln  coreASource
    //   lo  coreBSource
    //   lp  coreCSource
    //   lq  envSource
    //   lr  clientSessionId
    //   ls  reportedAt (long, millis)
    //   lt  launcherVersion
}
```

Sent proactively by the server after auth completes (via `sendSecurityProfile`).
FKCAC just needs an empty handler; this is server-initiated, not a client request.

---

## 8. Auth Flow (Complete)

```
[Client] → SPacketHello (0)
[Server] → SPacketHello (salt, nonce, flags)
[Client] → CPacketHelloReply (version, salt, ld, le, lf)   ← handshake
[Client] → ClientAuthHelloPacket (clientId)                ← auth start
[Server] → ServerAuthChallengePacket (challenge)           ← optional challenge
[Client] → ClientAuthResponsePacket (clientId, salt, response)
[Server] → ServerAuthResultPacket (authorized, result)
[Server] → CPacketSecurityProfile (15)                     ← system profiling (optional)
```

---

## 9. Why the Original "客户端已过期" Error Occurred

The server requires all three fingerprint fields (ld, le, lf) in CPacketHelloReply.
Without them the server-side validation sees an incomplete reply and treats the client
as running an outdated protocol version, issuing the kick message.

Additionally, SPacketHello must be fully consumed (3 fields) before responding;
if only 1 byte is read, subsequent wire positions become desynchronized.

---

## 10. Implementation Plan

| File | Change |
|------|--------|
| `SPacketHello.java` | Add `challengeNonce` (int), `challengeFlags` (int) fields; update `fromBytes` |
| `CPacketHelloReply.java` | Add `ld`, `le`, `lf` fields; update constructors and `toBytes` |
| `HandshakeChallenge.java` (new) | Implement SHA-1 challenge-response |
| `AuthUtils.java` (new) | Implement `buildAuthResponse`, expose hardcoded salt/clientId |
| Auth packets (new) | `ClientAuthHelloPacket`, `ServerAuthChallengePacket`, `ClientAuthResponsePacket`, `ServerAuthResultPacket` |
| `SecurityProfilePacket.java` (new) | Empty-stub class for discriminator 15 registration |
| `ProtocolUtils.java` | Add `getClientIntegrityFingerprint()`, `getClientClassSourceFingerprint()` |
| `FKCACProtocolHandler.java` | Update HelloHandler with full computation; add auth handlers; add empty handlers |
| `FKCAC.java` | Register discriminators 11–15 |
| `SpoofConfig.java` | Add `authEnabled` toggle, `hardcodedSalt` override |
