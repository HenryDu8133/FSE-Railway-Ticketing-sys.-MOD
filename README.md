# FSE Railway Ticketing System FSE铁路运输票务系统

基于 **NeoForge (Minecraft 1.21.1)** 平台开发的 Minecraft 铁路售票系统模组。

## 🚂 功能特性

* **多类型票卡**: 包含区间票 (Local Ticket)、特快票 (Exp Ticket)、单程票 (Single-trip Ticket) 以及 IC 交通卡，支持动态 NBT 数据展示（如乘车区间、票价、乘次、票号及状态）。
* **智能闸机与售票机**:
  * 包含检票机 (Ticket Inspection Machine) 和售票机 (Ticket Vending Machine) 方块。
  * 支持自定义 GUI 与票务处理逻辑。
* **ComputerCraft (CC) 深度联动**:
  * 机器设备可作为 CC 电脑的外设 (Peripheral) 接入网络。
  * 提供 Lua API（如 `markEntered`, `markExited`, `resetTicketState` 等）供玩家通过编程控制进出站逻辑并实时更新票卡 NBT 数据。

## 🛠️ 运行与开发环境

* **Minecraft**: 1.21.1
* **Mod 加载器**: NeoForge (21.1.219+)

