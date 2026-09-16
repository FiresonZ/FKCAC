# FKCAC

**FKCAC (Fuck CatAntiCheat)** —— 针对 CatServer / CatAntiCheat 服务端检测的 **Minecraft 1.7.10 Forge 客户端 Mod**。

本项目用于**自建服务器反作弊鲁棒性测试**：它接管 CatAntiCheat 的插件消息通道，并对服务端的各类校验**主动返回由客户端控制、伪装正常的应答**，从而验证反作弊是否存在可被绕过/伪造的检测链路。

> 仅面向自建服务器 / 测试环境。请在确认权限后再在他人服务器上使用。

---

## 参考项目

| 角色 | 仓库 | 说明 |
| --- | --- | --- |
| **本体（被对抗的反作弊）** | [Luohuayu/CatAntiCheat-Public](https://github.com/Luohuayu/CatAntiCheat-Public) | CatServer 附带的客户端检测 Mod，定义了本 Mod 需要对抗的插件消息协议（`CatAntiCheat` 通道，消息 0~10），以及加入时的 FML modlist 校验。 |
| 功能参考 | [Cat7373/Anti-AntiCheat-MOD](https://github.com/Cat7373/Anti-AntiCheat-MOD) | 1.7.x Forge 客户端对抗 Mod：接管通道、伪造哈希列表、可配置。本项目“删除原 mod、以其 modid 顶替”的思路由此而来。 |
| 功能参考 | [HaHaWTH/ACSucks](https://github.com/HaHaWTH/ACSucks) | C# 版多反作弊（CatAntiCheat / ASAC / ModList）破解工具，用于理解插件消息与哈希/截图伪造的思路。本仓库为 1.7.10 Forge 纯客户端实现。 |
| 加固参考 | [AlphaAutoLeak/AntiCatAnticheat](https://github.com/AlphaAutoLeak/AntiCatAnticheat) | 更深入的反反作弊：用 ASM coremod 在 `SimpleNetworkWrapper.sendToServer` / `SimpleChannelHandlerWrapper.channelRead0` 边界插桩拦截并伪造消息，避免“替换 mod”的指纹暴露。其核心思想作为本项目后续的类名指纹对抗 / 拦截加固方向。 |

---

## 对抗的检测链路（CatAntiCheat 1.7.10）

CatAntiCheat 客户端 Mod（`CatAntiCheat-1.7.10`）通过 `SimpleNetworkWrapper` 在通道 `CatAntiCheat` 上与服务端插件交互。服务端会发起以下校验，客户端必须如实应答，否则会被标记/拉黑：

| 判别符 | 方向 | 消息 | 检测内容 | FKCAC 对策 |
| --- | --- | --- | --- | --- |
| 加入时 | — | — | **FML 握手 modlist 必须含键 `catanticheat`**，否则判定“客户端未安装反作弊” | 本 Mod 的 modid 即 `catanticheat`，作为原 mod 的顶替件通过加入校验 |
| 0 | S→C | `SPacketHello` | 握手，下发随机 salt | 回 `CPacketHelloReply` 回显 salt、上报协议版本 `2`（服务端校验相等） |
| 1 | S→C | `SPacketFileCheck` | 请求文件哈希列表（要求 **>5 条、格式 `40hexSHA1\0name`**） | 回 `CPacketFileHash`，默认上报真实启动源哈希（排除自身、均在允许名单），可用配置覆盖 |
| 2 | S→C | `SPacketClassCheck` | 请求检测可疑类是否存在；包含服务端“标记类”，要求其被发现 | 回 `CPacketClassFound`，按“类是否真实可加载”上报，保证标记类被报为已发现、伪造的作弊类不被报 |
| 3 | S→C | `SPacketScreenshot` | 请求截屏 | 回 `CPacketImageData`，默认发送空白 PNG |
| 9 | S→C | `SPacketDataCheck` | 周期性重检注入类 / 原版渲染标志 | 回 `CPacketVanillaData`，报告健康客户端标志 |
| 4,5,6,7,8,10 | C→S | `CPacket*` | （客户端上报，服务于上述请求） | 注册类型以便序列化发送 |

协议的序列化格式忠实复刻自 CatAntiCheat-Public 的 `CatAntiCheat-1.7.10` 消息类，并与 `CatAntiCheatPlugin` 服务端 `PlayerHandler` 的判定逻辑逐项核对。

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

工作流 [`.github/workflows/build.yml`](.github/workflows/build.yml) 为**纯手动触发**（`workflow_dispatch`，不会随 push/PR/tag 自动运行）：

- 在仓库 **Actions** 页手动点击运行即可执行 `./gradlew build` 并上传构建产物。
- 若在某个 `v*` tag 上手动触发（Run workflow 时把分支切到该 tag），还会自动创建对应的 GitHub **Release** 并附带打包好的 Mod 文件。

---

## 使用

1. 关闭游戏，**删除 `mods/` 下的原 CatAntiCheat 客户端 Mod**（本 Mod 占有 `catanticheat` 这一 modid 与 `CatAntiCheat` 通道，原 mod 与它并存会让 FML 报重复 modid / 通道冲突）。
2. 将 `FKCAC-<version>.jar` 放入客户端的 `mods/` 目录。
3. 使用带 Forge 的 1.7.10 客户端启动并进入服务器。

## 配置

配置文件位于 `config/fkcac.cfg`，可在不重启客户端的情况下修改后重进服务器生效：

| 项 | 默认 | 说明 |
| --- | --- | --- |
| `spoofScreenshot` | `true` | 是否用空白 PNG 应答截屏请求 |
| `fileHashList` | 空 | 文件校验上报的哈希列表，每行一条 `40hexSHA1\0名称`；留空则自动上报真实启动源哈希（排除自身 jar，均在服务器允许名单内） |

> 若你新增了服务器名单外的 mod，建议先在干净客户端下记录一次 `fileHashList` 填入配置，再继续修改客户端，避免文件校验把新 mod 判为“注入/自装 MOD”。

---

## 已知边界与加固方向

本仓库当前采用「**以 `catanticheat` 顶替原 mod 并接管通道**」的方式，已按服务端 `PlayerHandler` 逐项核对。它在大版本内有效，但仍存在以下可被对抗的边界：

1. **类名指纹**：协议里服务端可下发任意类名查询（`SPacketClassCheck`），本实现按“类是否真实可加载”作答——若服务端能获知 FKCAC 的类名并下发查询，就可能被识别。
2. **fileHashList 的允许名单依赖**：文件校验本质依赖服务器允许名单，脱离名单需人工维护配置。
3. **更深的注入对抗**：一旦大版本升级对“替换 mod”的通道/握手做校验，可参照 [AlphaAutoLeak/AntiCatAnticheat](https://github.com/AlphaAutoLeak/AntiCatAnticheat) 的思路，改用 **ASM coremod 在 `SimpleNetworkWrapper.sendToServer` / `SimpleChannelHandlerWrapper.channelRead0` 边界插桩**来拦截并伪造消息，从而无需替换原 mod，也规避类名/文件指纹。此部分留待后续迭代实现。

---

## 免责声明

本仓库仅用于**反作弊检测机制的研究与自建服务器测试**。破坏他人服务器规则属违反服务条款的行为，造成的后果由使用者自行承担。开发者在对抗 CatAntiCheat 相关机制时，建议基于自己拥有的服务器进行。