# OpenComputers 无人机联动

[English](opencomputers-drone-example-en.md) · [设备 English](drone-device-guide-en.md) · [舰队物流 English](drone-fleet-guide-en.md)

## 配对

玩家看向停机坪、无人机编程器或舰队控制器，在聊天栏执行：

```text
/drtechoc pair
```

命令只向设备所有者显示一次 48 位令牌。再次执行会轮换令牌并立即废止旧令牌；`/drtechoc revoke` 撤销，`/drtechoc status` 查询状态。撤销只禁用凭据，不会释放编程器或控制器的主人归属；拆除机器后才会清除归属。服务端只保存 SHA-256 哈希，机器被拆除后配对自动删除。

## 通用返回结构

所有受保护回调的第一个参数都是令牌，返回单个 Lua 表：成功为 `{ok=true,data={...}}`，失败为 `{ok=false,error="..."}`。查询页码从 0 开始，单页最多 64 条。组件每台电脑每秒最多调用 20 次。

```lua
local component = require("component")
local event = require("event")
local token = "把 /drtechoc pair 返回的令牌放在这里"
local fleet = component.drtech_drone_fleet

local result = fleet.queryDrones(token, 0, 16)
if not result.ok then error(result.error) end
for _, drone in ipairs(result.data.entries) do
  print(drone.id, drone.status, drone.energy, drone.energyCapacity)
end
```

## 停机坪 `drtech_drone_dock`

- `componentInfo()`：公开返回组件、坐标和是否已配对。
- `isPaired(token)`：验证令牌。
- `getDock(token)`：查询名称、占用、启用、红石、优先级和自动发射/回收状态。
- `launch(token)`、`recall(token)`：发射库存中的无人机或召回绑定无人机。
- `controlDockDrone(token, "START"|"STOP"|"RECALL")`：控制当前绑定无人机。

## 编程器 `drtech_drone_programmer`

- `listPrograms(token, page, size)`：分页读取本人以及明确授权给本人的程序。
- `compileProgram(token, transfer)`：校验 `DRTECH-PROGRAM-1:` 剪贴板格式并写入所有者程序库；冲突或越界会拒绝。
- `assignProgram(token, droneUuid, programUuid, revision)`：向已加载、空闲且硬件兼容的本人无人机分配程序；`revision=-1` 使用当前版本。

## 舰队 `drtech_drone_fleet`

```lua
local endpoints = fleet.queryEndpoints(token, "ITEM", 0, 16)
if not endpoints.ok then error(endpoints.error) end
for _, endpoint in ipairs(endpoints.data.entries) do
  print(endpoint.id, endpoint.kind, endpoint.online, endpoint.stored, endpoint.capacity)
end

local sourceId = endpoints.data.entries[1].id
local resources = fleet.queryEndpointResources(token, sourceId, 0, 16)
if not resources.ok then error(resources.error) end
for _, resource in ipairs(resources.data.entries) do
  print(resource.id, resource.amount, resource.capacity)
end

local submit = fleet.submitLogistics(token, "ITEM", "minecraft:iron_ingot", 64,
  "源端点 UUID", "目标端点 UUID", 10)
if not submit.ok then error(submit.error) end
local jobId = submit.data.job

local jobs = fleet.queryJobs(token, 0, 16)
if jobs.ok then
  for _, job in ipairs(jobs.data.entries) do print(job.id, job.state, job.stage or "-") end
end

-- fleet.cancelJob(token, jobId)
-- fleet.controlDrone(token, "无人机 UUID", "RECALL")
```

- `queryEndpoints(token, kind, page, size)`：分页查询端点；`kind` 可为 `ITEM`、`FLUID`、`EU` 或空字符串。
- `queryEndpointResources(token, endpointUuid, page, size)`：分页读取端点公布的真实资源 ID、数量和容量。
- `queryDrones`、`queryJobs`：分页查询无人机和任务。
- `submitLogistics`、`cancelJob`、`controlDrone`：提交物流、取消任务和控制无人机。

提交物流前会校验源端点和目标端点均属于配对所有者、资源类型相同且 UUID 不同。应直接使用 `queryEndpointResources` 返回的资源 ID，避免手工拼写物品元数据或流体名称。

## 事件信号

```lua
while true do
  local signal, id, a, b = event.pull()
  print(signal, id, a, b)
end
```

组件通过 `computer.signal` 发出 `drtech_drone_launch`、`drtech_drone_dock`、`drtech_drone_status`、`drtech_drone_error`、`drtech_drone_low_energy` 和 `drtech_drone_task_complete`。信号只在凭据有效且完成初始状态基线后报告变化，撤销令牌会停止信号，避免区块加载时伪造事件。

OC 是纯可选依赖；相关 API 类只通过反射入口加载，未安装 OC 时不会加载驱动类，核心无人机功能不依赖 OC。
