# OpenComputers Drone Integration

[中文说明](opencomputers-drone-example.md) · [Devices](drone-device-guide-en.md) · [Fleet logistics](drone-fleet-guide-en.md)

OpenComputers support is optional. DrTech loads its OC driver registration through an isolated reflective entry point only when OC is installed; the core drone system works without OC.

## Pairing and Security

Look at a drone dock, drone programmer, or fleet controller and run:

```text
/drtechoc pair
```

Only the device owner receives the 48-character token, shown once. Pairing again rotates it and invalidates the old token. `/drtechoc revoke` revokes it and `/drtechoc status` reports pairing state. The server stores only a SHA-256 hash. Breaking the device removes pairing data. Revocation disables the credential but does not transfer or clear device ownership.

Protected callbacks take the token as their first argument and return one Lua table:

```lua
{ok=true, data={...}}
{ok=false, error="..."}
```

Pages are zero-based and contain at most 64 entries. Each computer may make at most 20 component calls per second. Security-relevant operations are also audited; the bounded device audit retains up to 512 records.

## Component Discovery

Adjacent devices expose these component names:

```text
drtech_drone_dock
drtech_drone_programmer
drtech_drone_fleet
```

```lua
local component = require("component")
local token = "token returned by /drtechoc pair"
local fleet = component.drtech_drone_fleet

local result = fleet.queryDrones(token, 0, 16)
if not result.ok then error(result.error) end
for _, drone in ipairs(result.data.entries) do
  print(drone.id, drone.status, drone.energy, drone.energyCapacity)
end
```

## Dock Callbacks: `drtech_drone_dock`

- `componentInfo()` — public component identity, coordinates, and paired state.
- `isPaired(token)` — checks a credential.
- `getDock(token)` — name, occupancy, enabled/redstone state, priority, auto-launch, and auto-recall.
- `launch(token)` — launches the stored drone when normal dock checks pass.
- `recall(token)` — recalls the bound drone.
- `controlDockDrone(token, "START"|"STOP"|"RECALL")` — controls the currently bound drone.

## Programmer Callbacks: `drtech_drone_programmer`

- `listPrograms(token, page, size)` — pages through programs owned by or explicitly shared with the paired owner.
- `compileProgram(token, transfer)` — validates a `DRTECH-PROGRAM-1:` clipboard payload and imports the compiled program into the owner's library. Conflicts and bounds violations are rejected.
- `assignProgram(token, droneUuid, programUuid, revision)` — assigns an authorized compiled revision to a loaded, idle, hardware-compatible owned drone. Use `revision=-1` for the current revision.

## Fleet Callbacks: `drtech_drone_fleet`

- `queryDrones(token, page, size)` — owner-scoped drone registry page.
- `queryJobs(token, page, size)` — owner-scoped persistent logistics jobs.
- `queryEndpoints(token, kind, page, size)` — endpoints filtered by `ITEM`, `FLUID`, `EU`, or an empty kind.
- `queryEndpointResources(token, endpointUuid, page, size)` — real resource IDs, amounts, and capacities published by one endpoint.
- `submitLogistics(token, kind, resourceId, amount, sourceUuid, targetUuid, priority)` — submits a validated job.
- `cancelJob(token, jobUuid)` — cancels an owned non-terminal job using the normal cargo-preserving path.
- `controlDrone(token, droneUuid, "START"|"STOP"|"RECALL")` — controls an online owned drone.

Use IDs returned by `queryEndpointResources`; do not hand-build item metadata or fluid IDs.

```lua
local endpoints = fleet.queryEndpoints(token, "ITEM", 0, 16)
if not endpoints.ok then error(endpoints.error) end

local sourceId = endpoints.data.entries[1].id
local resources = fleet.queryEndpointResources(token, sourceId, 0, 16)
if not resources.ok then error(resources.error) end

local resourceId = resources.data.entries[1].id
local submit = fleet.submitLogistics(token, "ITEM", resourceId, 64,
  sourceId, "target endpoint UUID", 10)
if not submit.ok then error(submit.error) end
print("job", submit.data.job)

for _, job in ipairs(fleet.queryJobs(token, 0, 16).data.entries) do
  print(job.id, job.state, job.stage or "-")
end
```

Submission verifies that both endpoint UUIDs are distinct, have the requested kind, and belong to the paired owner. Normal inventory, reservation, drone compatibility, and capacity checks still apply.

## Event Signals

```lua
local event = require("event")
while true do
  local signal, id, a, b = event.pull()
  print(signal, id, a, b)
end
```

Drivers emit:

```text
drtech_drone_launch
drtech_drone_dock
drtech_drone_status
drtech_drone_error
drtech_drone_low_energy
drtech_drone_task_complete
```

Signals begin only after an initial state baseline and while the credential remains valid, preventing fake change events during chunk load. Revocation stops signals.

## Deployment Verification

Because OC is a compile-only optional dependency, validate both modpack configurations before release: one game start/world load with OC absent, and another with the matching OC version installed, paired devices, callbacks, rate limiting, token rotation/revocation, and signals. These are runtime acceptance checks and should not be marked passed from compilation alone.
