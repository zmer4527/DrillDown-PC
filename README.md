# DrillDown (The Quarry)

基于 [Dakror](https://github.com/Dakror) 的开源工厂建造游戏 **Drill Down** 的衍生（fork）工程。
本仓库在原有桌面端代码的基础上，做了**确定性模拟改造**并新增了**局域网联机**功能。

- 上游原仓库：<https://github.com/Dakror/DrillDown>
- 授权：Apache License 2.0（见 [LICENSE](LICENSE)、[NOTICE](NOTICE)）

---

## 一、原项目（版权归原作者 Dakror）

**Drill Down** 是 Dakror 开发的工厂建造游戏，2019 年发布于 Steam 与 Google Play。
作者于 2024 年 8 月将游戏源码开源在 GitHub 上。

> 以下为原仓库 `README.md` 原文（版权归原作者所有，为完整保留与署名而收录）：

> # Drill Down
>
> This is the open-sourced codebase of the factory building game Drill Down, released in 2019 on Steam and Google Play.
>
> Binaries for Android and PC are available at [itch.io](https://dakror.itch.io/drill-down).
>
> ## Building from source
>
> The game needs 64-bit Java >= 11 and Android Studio to compile.
>
> 1. Create a java keystore for code signing
> 2. Enter the credentials into `gradle.properties`
> 3. For Android run `gradlew android:assembleFullRelease`, for PC run `gradlew desktop:dist` to get the runnable binaries

原仓库：<https://github.com/Dakror/DrillDown>

---

## 二、本仓库的改动

在原作者代码基础上（本工程为反编译重建的 Gradle / Java 8 桌面工程）新增/修改了：

- **确定性固定步长模拟**：固定 60Hz tick + 累积器；随机数改为集中式 `SimRandom`（`RandomXS128`），
  消除 `Math.random()` 造成的不可复现行为；渲染路径不再改写地图状态；库存容器改用 `EnumMap`、
  序列化时对 key 排序，保证迭代顺序确定。
- **局域网联机**（`de.dakror.quarry.net` 包）：基于 TCP 的**权威锁步（lockstep）**方案——
  主机播种世界，双端按 tick 交换命令包（含输入延迟）并周期性比对状态哈希，出现分叉时由主机重发世界。
  放置/删除建筑、暂停、倍速等操作改为命令同步。
- **界面调整**：建造菜单层级布局、窗口拖动等。

> 说明：本仓库为**桌面端**工程（仅 LWJGL2 后端）。上文中原作者的 Android 构建说明（`android:assembleFullRelease`、`gradle.properties` 签名等）**不适用于本仓库**。

### 联机快捷键

| 按键 | 功能 |
| --- | --- |
| `F7`  | 断开当前联机会话（回到单机模拟） |
| `F8`  | 作为主机开房（默认端口 `25565`，控制台会打印本机 IP） |
| `F9`  | 输入 `ip[:端口]` 加入主机 |
| `F10` | 切换确定性锁步模式（会写入偏好设置，持久保存） |
| `F11` | 存档 round-trip 自检（序列化→解析→比对，前后 `simHash` 应相等） |
| `F12` | 打印当前 `simHash`（用于排查分叉） |

原有的调试键（如 `F1` 切换 UI 显示、`F4` 平滑相机）保持原样。

---

## 三、构建与运行（本仓库）

需要 64 位 JDK 8。

```bash
# 构建（生成可运行 jar：build/libs/DrillDown-1.0.jar）
./gradlew build          # 离线环境可加 --offline

# 运行
java -jar build/libs/DrillDown-1.0.jar
# 或
./gradlew run
```

主类：`de.dakror.quarry.desktop.DesktopLauncher`

---

## 四、许可与致谢

- 本项目基于 **Drill Down**（作者 **Dakror**）开发，原作者版权：`Copyright 2017 Dakror`。
- 依 **Apache License 2.0** 授权发布，许可证全文见 [LICENSE](LICENSE)，署名与改动说明见 [NOTICE](NOTICE)。
- 本仓库的改动由 **zmer4527** 维护。

感谢原作者 Dakror 将这款游戏的源码开源。
