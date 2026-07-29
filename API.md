# FSE Ticketing CC  API

## 通用说明

- 外设类型通过 `peripheral.getType(name)` 返回，或在 `peripheral.wrap(name)` 后通过 `peripheral.call`/方法调用使用。
- 返回值约定：
  - 成功通常返回 `true`（部分方法额外返回数据）。
  - 失败通常返回 `false, "error_message"` 或 `nil, "error_message"`（取决于具体方法）。
- 票/卡信息表（Lua table）中的字段 key 与模组 NBT key 一致，详见“数据字段”章节。

## 外设：ticket\_vending\_machine（售票机）

### 方法：issueTicket

发放一张票并从售票机正面吐出（生成掉落物）。

```lua
local ok, ticketId = vending.issueTicket(
  startNameEn,
  terminalNameEn,
  type,
  rides,
  cost,
  startStation,
  terminalStation,
  fromNameCnU,
  toNameCnU
)
```

- 参数（均为可选，未提供时使用默认值）
  - `startNameEn`：起点（英文/标识），默认 `"???"`
  - `terminalNameEn`：终点（英文/标识），默认 `"???"`
  - `type`：票种，默认 `"local"`
    - `"local"`：普通票（Local Ticket）
    - `"limited_express"`：特急票（Exp Ticket）
    - `"single"`：单程票（Single-trip Ticket）
  - `rides`：可乘坐次数，默认 `1`，最小会被钳制为 `1`
  - `cost`：金额，默认 `0`
  - `startStation`：起点站 ID/代码，默认 `""`
  - `terminalStation`：终点站 ID/代码，默认 `""`
  - `fromNameCnU`：中文起点名（可能包含 `\u` 转义），默认 `""`
  - `toNameCnU`：中文终点名（可能包含 `\u` 转义），默认 `""`
- 返回
  - `ok`：`true`
  - `ticketId`：生成的票 ID（形如 `AB-12345678`）

### 方法：issueICCard

发放一张 IC 卡并从售票机正面吐出（生成掉落物）。

```lua
local ok, cardId = vending.issueICCard(ownerName, balance)
```

- 参数（可选）
  - `ownerName`：持有人名字，默认 `""`
  - `balance`：初始余额，默认 `0`
- 返回
  - `ok`：`true`
  - `cardId`：生成的卡号（形如 `IC-xxxxxxxx`）

### 方法：issueFSEPass

```lua
local r1, r2 = vending.issueFSEPass(...)
```

当前版本仅在 `getMethodNames()` 中声明，尚未在逻辑中实现分发，调用会直接返回空结果（无返回值）。

## 外设：ticket\_inspection\_machine（检票机）

检票机通过玩家右键扫描票/卡，保存“最近一次扫描”的数据，并向连接的 CC 电脑推送事件。

### 方法：getLastScanned

获取最近一次扫描到的票/卡信息表。

```lua
local infoOrNil, err = gate.getLastScanned()
```

- 返回（成功）
  - `infoOrNil`：Lua table（票信息或 IC 信息）
- 返回（失败）
  - `nil, "no ticket scanned"`

### 方法：destroyTicket

销毁最近一次扫描的物品（直接清空玩家手上那一张票/卡）。

```lua
local ok, err = gate.destroyTicket()
```

- 返回
  - `true`：已投递到服务器主线程执行
  - `false, "not server level"`：当前世界不是服务端世界

注意：该方法当前实现为“投递执行”，不会把实际销毁结果回传给 Lua；建议通过事件与 `getLastScanned()` 侧面确认状态。

### 方法：deductICCard

对最近一次扫描到的 IC 卡扣费。

```lua
local ok, err = gate.deductICCard(amount)
```

- 参数
  - `amount`：扣费金额（double）
- 返回
  - `true`：已投递到服务器主线程执行
  - `false, "not server level"`：当前世界不是服务端世界

注意：当前实现同样不会把扣费后的余额回传给 Lua；如果需要余额，请调用 `getLastScanned()` 或使用充值机外设完成扣费/查询。

### 方法：markEntered

将最近一次扫描的票/卡标记为已进站，并（可选）写入进站站点。

```lua
local ok, err = gate.markEntered(entryStationId)
```

- 参数（可选）
  - `entryStationId`：站点 ID/代码，默认 `""`（为空则不写入）
- 返回
  - `true`：已投递到服务器主线程执行
  - `false, "not server level"`

执行后会推送 `ticket_state_updated` 或 `ic_card_state_updated` 事件（取决于最近扫描的是票还是卡）。

### 方法：markExited

将最近一次扫描的票/卡标记为已出站，并清理进站站点字段。

```lua
local ok, err = gate.markExited()
```

- 返回
  - `true`：已投递到服务器主线程执行
  - `false, "not server level"`

### 方法：resetTicketState

将最近一次扫描的票/卡状态复位（entered/exited 均为 false，并清理进站站点字段）。

```lua
local ok, err = gate.resetTicketState()
```

- 返回
  - `true`：已投递到服务器主线程执行
  - `false, "not server level"`

### 事件

检票机对所有已连接电脑（`attach` 过的电脑）推送以下事件。事件参数为 1 个 table：

- `ticket_scanned(info)`
- `ic_card_scanned(info)`
- `ticket_state_updated(info)`
- `ic_card_state_updated(info)`

`info` 结构见“数据字段”章节（包含 `passenger` 字段，值为扫描者玩家名）。

## 外设：ic\_refill\_machine（IC 充值机）

充值机要求玩家先把 IC 卡插入机器（右键把 IC 卡放进去）。只有在机器内有卡时，Lua 方法才可用。

### 方法：getCardInfo

```lua
local infoOrNil, err = refill.getCardInfo()
```

- 返回（成功）
  - `infoOrNil`：Lua table（IC 卡信息）
- 返回（失败）
  - `nil, "no card"`：机器内没有插卡

### 方法：refill

```lua
local okOrNil, r2 = refill.refill(amount)
```

- 参数
  - `amount`：充值金额（double）
- 返回（成功）
  - `true, newBalance`
- 返回（失败）
  - `nil, "no card"`

### 方法：deduct

```lua
local okOrNil, r2 = refill.deduct(amount)
```

- 参数
  - `amount`：扣费金额（double）
- 返回（成功）
  - `true, newBalance`
- 返回（失败）
  - `nil, "no card"`
  - `false, "insufficient"`：余额不足（扣费后会变成负数时阻止）

## 数据字段（info table keys）

### 票信息（ticket\_\* / ticket\_state\_updated / getLastScanned 返回为票时）

- `start_name_en`：起点名（英文/标识）
- `terminal_name_en`：终点名（英文/标识）
- `start_station`：起点站 ID/代码
- `terminal_station`：终点站 ID/代码
- `fromNameCnU`：中文起点名（可能包含 `\u` 转义）
- `toNameCnU`：中文终点名（可能包含 `\u` 转义）
- `type`：票种（`local` / `limited_express` / `single`）
- `rides`：剩余/可用次数
- `entered`：是否已进站
- `exited`：是否已出站
- `ticketId`：票 ID
- `timestamp`：签发时间戳（毫秒）
- `cost`：金额
- `order_datetime`：下单/签发时间字符串（`yyyy-MM-dd HH:mm:ss`）
- `passenger`：扫描者玩家名（仅检票机事件与 getLastScanned 返回中提供）

注意：单程票的 `line_name` 当前不在 `info` table 中返回（仅保存在票的 NBT 内）。

### IC 信息（ic\_\* / ic\_card\_state\_updated / getLastScanned 返回为卡时）

- `cardId`：卡号
- `ownerName`：持有人
- `balance`：余额
- `entered`：是否已进站
- `entry_station`：进站站点 ID/代码（可能为空）
- `passenger`：扫描者玩家名（仅检票机事件与 getLastScanned 返回中提供）

