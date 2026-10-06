# Drill Down

工厂建造游戏 **Drill Down** 的反编译桌面重制版（fork）。

## 原项目

- 原作者：**Dakror**（Dakror Games）
- 2019 年发布于 Steam 与 Google Play，2024 年作者将源码开源
- 许可证：Apache License 2.0（Copyright 2017 Dakror）
- 原仓库：<https://github.com/Dakror/DrillDown>
- 可执行版本：[itch.io](https://dakror.itch.io/drill-down)

## 本仓库的改动

- 基于原版反编译（CFR）重建的**桌面版**工程（Gradle / Java 8 / libGDX 1.9.9，LWJGL2 后端），仅保留桌面端。

## 构建与运行

需要 64 位 JDK 8。

```bash
./gradlew build                          # 生成 build/libs/DrillDown-1.0.jar
java -jar build/libs/DrillDown-1.0.jar   # 运行
# 或
./gradlew run
```

主类：`de.dakror.quarry.desktop.DesktopLauncher`

> 注意：原版 README 中的 Android 构建说明（`gradlew android:assembleFullRelease`、代码签名等）不适用于本仓库 —— 本仓库为桌面版工程。

## 许可证

基于 Dakror 的 **Drill Down**（Apache License 2.0），原版权归 Dakror 所有。
