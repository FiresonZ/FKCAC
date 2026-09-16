# FKCAC

**FKCAC (Fuck CatAntiCheat)** —— 针对 CatServer / CatAntiCheat 服务端检测的 **Minecraft 1.7.10 Forge 客户端 Mod**。

本项目用于**自建服务器反作弊鲁棒性测试**：它接管 CatAntiCheat 的插件消息通道，并对服务端的各类校验**主动返回由客户端控制、伪装正常的应答**，从而验证反作弊是否存在可被绕过/伪造的检测链路。

> 仅面向自建服务器 / 测试环境。请在确认权限后再在他人服务器上使用。

---

## 参考项目

| 角色 | 仓库 | 说明 |
| --- | --- | --- |
| **本体（被对抗的反作弊）** | [Luohuayu/CatAntiCheat-Public](https://github.com/Luohuayu/CatAntiCheat-Public) | CatServer 附带的客户端检测 Mod，定义了本 Mod 需要对抗的插件消息协议（`CatAntiCheat` 通道，消息 0~10）。 |
| 功能参考 | [Cat7373/Anti-AntiCheat-MOD](https://github.com/Cat7373/Anti-AntiCheat-MOD) | 1.7.x Forge 客户端对抗 Mod：接管通道、伪造 MD5 列表、可配置哈希。本项目的配置与“删除原 mod 后用本 mod 顶替”的思路由此而来。 |
| 功能参考 | [HaHaWTH/ACSucks](https://github.com/HaHaWTH/ACSucks) | C# 版多反作弊（CatAntiCheat / ASAC / ModList）破解工具，用于理解插件消息与 NBT/哈希伪造的思路。本仓库为 1.7.10 Forge 纯客户端实现。 |

---

## 对抗的检测链路（CatAntiCheat 1.7.10）

CatAntiCheat 客户端 Mod（`CatAntiCheat-1.7.10`）通过 `SimpleNetworkWrapper` 在通道 `CatAntiCheat` 上与服务端插件交互。服务端会发起以下校验，客户端必须如实应答，否则会被标记/拉黑：

| 判别符 | 方向 | 消息 | 检测内容 | FKCAC 对策 |
| --- | --- | --- | --- | --- |
| 0 | S→C | `SPacketHello` | 握手，下发随机 salt | 回 `CPacketHelloReply`，报告伪装协议版本并回显 salt |
| 1 | S→C | `SPacketFileCheck` | 请求文件/MD5 列表 | 回 `CPacketFileHash`，使用可配置的伪造哈希列表 |
| 2 | S→C | `SPacketClassCheck` | 请求检测可疑类是否存在 | 回 `CPacketClassFound`，默认报告“全部未发现” |
| 3 | S→C | `SPacketScreenshot` | 请求截屏 | 回 `CPacketImageData`，默认发送空白 PNG |
| 9 | S→C | `SPacketDataCheck` | 周期性重检注入类 / 原版渲染标志 | 回 `CPacketVanillaData`，报告健康客户端标志 |
| 4,5,6,7,8,10 | C→S | `CPacket*` | （客户端上报，服务于上述请求） | 注册类型以便序列化发送 |

协议的序列化格式忠实复刻自 CatAntiCheat-Public 的 `CatAntiCheat-1.7.10` 消息类。

---

## 目录结构

```
.
├── .github/workflows/build.yml   # GitHub Actions：CI 构建 + 打 tag 自动发 Release
├── build.gradle.kts              # RetroFuturaGradle (1.7.10) 构建配置
├── settings.gradle.kts
├── gradle.properties
├── gradle/wrapper/               # Gradle Wrapper (Gradle 9.5.0)
└── src/main/
    ├── java/com/fkcac/
    │   ├── FKCAC.java            # 主 Mod：注册 "CatAntiCheat" 通道与全部消息
    │   ├── hook/SpoofConfig.java # 可配置伪造行为（哈希列表 / 截屏 / 类隐藏）
    │   └── network/
    │       ├── FKCACProtocolHandler.java # 各 S→C 请求的应答 handler
    │       └── message/          # CPacket*/SPacket* 协议消息类
    └── resources/mcmod.info
```

---

## 构建

### 环境要求

- 本工程面向 **Minecraft 1.7.10**（Forge 10.13.4.1614）。
- 构建工具链：**Java 8**（由 Toolchain 自动提供），**Gradle 9.5.0**（由 Wrapper 提供）。
- 依赖 RetroFuturaGradle 解析 MC/Forge/MCP，首次构建需联网下载。

### 本地构建

```bash
./gradlew build
```

产物在 `build/libs/`，主 jar 为不带 `-dev` / `-api` 后缀的 `FKCAC-<version>.jar`。

### CI 发布（GitHub Actions）

工作流 [`.github/workflows/build.yml`](.github/workflows/build.yml)：

- 在 `main` / `codex` 分支的 push 和 PR 上执行 `./gradlew build`，并上传构建产物。
- 推送形如 `v*` 的 tag 时，在构建成功后自动创建 GitHub **Release** 并附带打包好的 Mod 文件：
  ```bash
  git tag v1.0.0 && git push origin v1.0.0
  ```

---

## 使用

1. 关闭游戏，**删除 `mods/` 下的原 CatAntiCheat 客户端 Mod**（本 Mod 需要占用 `CatAntiCheat` 通道，二者并存会导致 FML 报通道冲突）。
2. 将 `FKCAC-<version>.jar` 放入客户端的 `mods/` 目录。
3. 使用带 Forge 的 1.7.10 客户端启动并进入服务器。

## 配置

配置文件位于 `config/fkcac.cfg`，可在不重启客户端的情况下修改后重进服务器生效：

| 项 | 默认 | 说明 |
| --- | --- | --- |
| `spoofScreenshot` | `true` | 是否用空白 PNG 应答截屏请求 |
| `hideAllQueriedClasses` | `true` | 是否将类检测一律应答为“未发现” |
| `fileHashList` | 空 | 文件校验时上报的哈希列表（每行一条）；留空则使用内置占位值 |

---

## 免责声明

本仓库仅用于**反作弊检测机制的研究与自建服务器测试**。破坏他人服务器规则属违反服务条款的行为，造成的后果由使用者自行承担。开发者在对抗 CatAntiCheat 相关机制时，建议基于自己拥有的服务器进行。