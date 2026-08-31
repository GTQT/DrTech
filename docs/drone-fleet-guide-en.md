# DrTech Fleet and Automatic Logistics Guide

[设备中文说明](drone-device-guide.md) · [Devices](drone-device-guide-en.md) · [Nodes](drone-programming-reference-en.md) · [OpenComputers](opencomputers-drone-example-en.md)

This document describes the native item, fluid, and EU logistics loop implemented by fleet controllers, endpoints, docks, and deployed drones.

## 1. Components and Ownership

- An EV Fleet Controller provides the owner UI, dispatch service, job history, and route snapshot.
- Item, Fluid, and EU endpoints publish stable UUIDs, real resource snapshots, policy settings, and heartbeats.
- HV/EV/IV docks provide binding, fallback recovery, charging, deployment, and storage.
- Deployed drones publish heartbeats to the persistent registry. Fleet Communication shortens the heartbeat period and extends wireless control range.

Every query and mutation is scoped to the requesting player. Controllers cannot enumerate another player's endpoints, drones, reservations, or jobs. Source, target, drone, task, dock, and cancellation ownership are revalidated server-side.

## 2. Endpoint Configuration

Configure each endpoint with:

- resource whitelist (up to 64 stable IDs);
- requested target stock;
- maximum amount offered per job;
- minimum stock kept at a source;
- maximum total stock accepted at a target;
- priority.

A request of zero creates no automatic demand. A zero provide/max-stock policy means no additional policy limit, not infinite physical storage. Fluid tanks still accept only a compatible fluid and all endpoints respect their real capability capacity.

Endpoints immediately republish after inventory, configuration, or owner changes and also send periodic heartbeats. Stale entries become offline rather than being treated as empty.

## 3. Discovery, Reservation, and Dispatch

For each demand, the controller searches online endpoints with the same owner, dimension, and resource kind. It applies resource filters, priority, source reserve, target request, offer caps, maximum stock, physical capacity, and all existing reservations.

The source and target amounts are reserved atomically under one job ID. Item reservations count each resource separately; target admission also accounts for the endpoint's total shared capacity, so different resources cannot overbook a mixed inventory.

The dispatcher selects a compatible online drone with usable battery and cargo capacity. Fluid jobs require Fluid Cargo; EU jobs require EU Interface. Zero-energy drones and item drones with no free cargo slot are excluded.

## 4. Job Lifecycle

Typical lifecycle:

`QUEUED → RESERVED → ASSIGNED → PICKUP → IN_TRANSIT → DELIVERY → COMPLETED → RETURNING/DOCKED`

Temporary failures enter `RETRY_WAIT` with bounded exponential backoff. The controller UI shows the remaining retry countdown. A successful retry clears the previous error text while retaining retry count for history.

Pick-up and delivery are real, ticked capability operations. They trigger tool-arm animation and resource-specific particles/sounds and charge EU based on the amount actually moved. Empty sources and full targets consume no transfer-action EU.

## 5. Multi-trip and Cargo Preservation

If a reservation exceeds one drone load, the same job repeatedly performs pick-up and delivery while tracking `picked`, `delivered`, and requested totals. Emptying the first load does not finish the job early.

Once a resource enters a drone it remains associated with that carrier. Other drones cannot claim the accounting entry. Cancellation, final retry exhaustion, endpoint loss, path failure, or restart releases unpicked reservations; picked cargo returns on its carrier to the bound dock or a compatible fallback dock.

If the drone is manually recovered, its cargo is serialized into the drone item and the task becomes recovered. If destroyed, item/fluid/transport-EU payload remains in the same-UUID dropped drone item, and the task immediately enters standard recovery handling. Native fleet logistics never uses the user's destructive “drop one stack” cargo policy.

## 6. Restart, Offline, and History Behavior

Jobs, registrations, reservations, retry state, and endpoint directory data are saved with the world. On server restart, in-progress work releases obsolete reservations and resumes through retry rather than duplicating inventory. Invalid or orphan reservation records are sanitized and removed during load.

The queue has a 2,048-entry hard cap. When accepting new jobs, the oldest terminal history may be reduced to 1,792 entries; active jobs and terminal jobs whose carrier is still returning are never deleted by history cleanup. UI synchronization prioritizes active jobs, then cargo-return jobs, then terminal history.

## 7. Controller UI and World Feedback

The controller lists up to 128 synchronized jobs with state/stage, resource, source/target, amount progress, reservation, carrier, retries, countdown, and failure text. Non-terminal jobs expose owner-checked cancellation. Up to 128 active/returning routes are shown as gold lines in their own dimension and only to the authorized viewer.

Drone records show online/offline state, dimension, position, chassis, flight battery, cargo usage, program revision, bound dock, and UUID. Remote START/STOP/RECALL requires an online loaded entity and passes distance, owner, and identity checks.

## 8. Failure Checklist for Manual Acceptance

The implementation exposes deterministic recovery for these cases, but a release build should still exercise them in game:

1. Fill the target after reservation but before delivery; verify retry and no loss.
2. Empty or replace the source before pickup; verify reservation release.
3. Unload source, target, carrier, and dock chunks at each job stage.
4. Restart the server during pickup, transit, delivery, retry wait, and cargo return.
5. Cancel before pickup and after pickup; verify only the second returns cargo.
6. Attempt cross-owner query, control, reservation, and cancellation.
7. Destroy and redeploy a loaded carrier; verify UUID and payload recovery.
8. Transfer more than one chassis load for item, fluid, and EU jobs.
9. Run many simultaneous drones and confirm reservation totals never exceed real capacity.

These scenarios remain manual acceptance work until actually run; documentation of expected behavior is not itself a passed test.
