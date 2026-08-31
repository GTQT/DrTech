# DrTech EU Drone Device Guide

[中文版](drone-device-guide.md) · [Node guide](drone-programming-reference-en.md) · [Fleet logistics](drone-fleet-guide-en.md) · [OpenComputers](opencomputers-drone-example-en.md)

This guide covers the native DrTech EU drone system. Device names are localized in game; the old `Drone Pad` is not an entry point for this system.

## 1. EV Drone Programmer

The programmer edits program cards, compiles graphs, and writes validated programs to drones.

1. Supply EV power or higher.
2. Insert a blank **Drone Program Card** in the upper-left slot. A new card starts with Start and End nodes.
3. Add nodes from the library. Click an output port and then a compatible input port to connect them; right-click a port to disconnect it.
4. Configure the selected node in the Properties tab. Resolve every red node and error in Diagnostics.
5. Insert a programmable drone in the upper-right slot and choose **Write to drone**. A successful write costs `8192 EU`.

The editor supports box/multi-selection, graph copy/paste, one-step transactional undo, alignment/distribution, grid snapping, automatic layout, minimap navigation, zoom-to-selection, aliases, breakpoints, groups, comments, and a persistent 256-entry debug timeline. The Diagnostics page can filter by severity and centers the referenced node when clicked. A failed compile consumes neither energy nor the target drone.

For coordinate and area constants, choose **Select in world** instead of typing coordinates: right-click one block for a point or two corners for an area. Sneak-right-click cancels. The server validates and writes the result directly to the card in the programmer.

The Templates page contains ten server-owned starting graphs: basic harvest, mining, transport, hybrid farming, region mining, item/fluid/EU logistics, GT maintenance, and fleet haul/return. A template consumes one blank card and is compiled before it enters the owner's program library. Template coordinates are placeholders and must be replaced with world selection.

## 2. EV Drone Fleet Controller

The fleet controller shows the owner's registered drones and persistent logistics jobs. It requires EV power.

- The drone page reports chassis, online/running state, dimension, coordinates, battery, cargo, program revision, bound dock, and UUID.
- An online selected drone can be started, stopped, or recalled. Distance, owner, dimension, heartbeat, and entity identity are checked by the server.
- The job page shows up to 128 jobs with type, state/stage, resource ID, delivered/picked/requested amount, reservation, endpoints, assigned drone, retry count, retry countdown, and localized failure reason.
- A non-terminal job can be cancelled from the UI. Cancellation preserves already-picked cargo and sends its carrier to a compatible owner dock.
- Current-dimension routes are rendered as owner-only gold source-to-target lines. The controller has a dedicated purple console, screen, antenna, and beacon appearance.

## 3. HV / EV / IV Drone Docks

A dock is a home point, charger, launcher, and recovery device.

1. Use a dock at least equal to the drone's voltage tier and supply EU.
2. Put the drone in the left slot and a compiled program card in the right slot.
3. The dock synchronizes the card and charges the drone.
4. Launch at 90% battery or enable automatic launch. Disabled docks, owner mismatch, empty slots, insufficient charge, and spawn failure are reported explicitly.
5. Recall pauses the program while preserving the current node, variables, progress, inventory, and energy. A manually recalled drone remains paused after relaunch until **Resume** is selected. Low-energy firmware return resumes automatically after charging.

HV, EV, and IV docks are intended for the matching chassis. A higher-tier dock may service a lower-tier drone. Dock ownership prevents other players from occupying or controlling it.

## 4. EV Logistics Endpoints

On first use every endpoint binds to its owner and registers a stable UUID, kind, dimension, coordinates, configuration, and resource snapshot.

| Endpoint | Real capability | Drone requirement |
| --- | --- | --- |
| Item | 9-slot Forge item buffer | free item cargo space |
| Fluid | 64,000 mB Forge tank | Fluid Cargo module |
| EU | EV-tier GT energy buffer | EU Interface module; transported EU is separate from flight energy |

Configure requested stock, per-job provide limit, priority, minimum reserve, maximum stock, and a whitelist of up to 64 resource IDs. A request of zero disables automatic demand. A zero policy limit means “no extra policy cap”; physical capacity is always enforced. Buttons change item values by `1/16/64`, fluid values by `100/1000/16000 mB`, and EU values by `1/4/64` EV packets with normal/Shift/Ctrl clicks.

The controller discovers online source and target endpoints owned by the same player, in the same dimension and of the same resource kind. It accounts for filters, priorities, real inventory, capacity, minimum reserves, provide limits, and existing reservations before atomically reserving a transfer.

Large reservations are split into lossless multi-trip runs. Pick-up and delivery consume flight energy according to the amount actually moved. Endpoint unload, restart, a full target, or path failure releases stale reservations and uses bounded exponential retry. Once cargo has been picked up it stays bound to that carrier; final failure or cancellation returns it to an owner dock instead of deleting it.

## 5. Drone Redstone Emitter

Place this endpoint beside a redstone device and target it with `set_redstone_output`. Strength `0..15` is emitted on all six faces; zero turns it off. The drone pays the action EU cost. The emitter stores only the output value and requires no cable.

## 6. Programmable Drones and Modules

HV/EV/IV chassis differ in energy, cargo, speed, range, health, armour, transfer batch size, and area capacity. Right-click a deployed drone for its control panel; sneak-right-click recovers it when allowed.

Modules expose hardware-gated features such as fluid cargo, EU transfer, crafting, advanced navigation, tool actions, entity sensing, combat, entity transport, waterproofing, self-repair, secure access, advanced item throughput, fleet communication, fishing, and Thaumcraft essentia transport. The programmer and runtime use the same hardware validator. Old programs remain readable if hardware is missing, but execution stops safely before energy use or world changes and names the required stable module ID.

When cargo becomes full, the owner can choose:

- **Stop** (default): automatic pickup stops and explicit nodes report `NO_SPACE`.
- **Drop one stack**: explicitly permits dropping the last valid cargo stack before retrying, with a pickup cooldown.
- **Return to dock**: pauses the program and follows the main/fallback safety-firmware dock chain.

Native fleet logistics always uses lossless multi-trip behavior and never applies the drop policy.

Combat modules expose two weapon slots and render the installed weapons below the claws. Fishing exposes a rod slot, can acquire a rod from drone cargo or a loaded nearby container, suspends the drone two blocks above the target water, and uses vanilla bobber, line, bite, loot, XP, enchantment, and durability rules. Thaumcraft essentia transport requires the Alchemy Upgrade and a jar in its dedicated slot; the drone scans a connected jar area or a default `5×5×5` volume around the furnace, prefers labelled non-full jars, renders the carried jar and aspect, and enforces at least 10 ticks between successful transfers.

## 7. Ownership and Safe Operation

- Programmer writes, world selection, dock use, fleet queries, endpoint discovery, remote control, reservations, and cancellation are owner-scoped and rechecked server-side.
- FakePlayer block/entity actions still pass normal protection and permission hooks.
- Low-energy firmware defaults to return at 20% and resume at 90%; programs may change these within validated bounds.
- Drone inventory, modules, dedicated equipment slots, transport EU/fluid, UUID, program revision, runtime state, and cargo-full policy survive normal recovery and destruction drops.
- World markers are informative only. They never bypass server ownership or permission checks.

See the [English node programming guide](drone-programming-reference-en.md) for graph rules and the [fleet logistics guide](drone-fleet-guide-en.md) for automatic transfer behavior.
