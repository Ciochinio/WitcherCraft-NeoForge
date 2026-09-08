# Milestone 4 points of interest implementation plan

Status: design plan, not implementation authorization  
Project: WitcherCraft for Minecraft 26.1.2 and NeoForge 26.1.2.95  
Java: 25  
Mod ID: `witchercraft`

## Purpose

Milestone 4 adds data-driven points of interest to the existing world map. A generated test structure comes first. It proves the world-generation and structure-detection path before persistence, networking, and map rendering depend on it.

The completed milestone must let two players independently reveal and discover the same generated structure. Opening or panning the map must never generate terrain, load a chunk, or reveal a location the server has not authorized.

This document replaces the temporary development marker block as the primary Milestone 4 test. Explicit marker blocks remain a planned provider type, but their implementation waits until real content needs them.

## Existing integration

Milestones 1 through 3 already provide:

- A locked `MapPage` inside the shared WitcherCraft GUI shell.
- A common world-to-screen transform for terrain and markers.
- Marker clipping, nearest-marker hover selection, and a reusable information card.
- Server-owned `SavedData` and bounded networking patterns in the waypoint system.
- A server-issued world UUID and connection-scoped client caches.
- `ChunkWatchEvent.Watch` handling for legitimately loaded Overworld chunks.

The implementation must extend these systems. It must not add another map screen, keybind, navigation shell, or fake MCreator GUI.

The attached master world-map plan contains old Milestone 3 assumptions about waypoint colors, tracking, sharing, and filters. The GDD, TDD, and master plan must be reconciled with the implemented code during this milestone.

## Player behavior

Each POI has two player-specific states:

- Revealed: the map shows an unknown question mark at an approximate position.
- Discovered: the map shows the exact position, icon, and name.

Initial behavior:

- Reveal distance defaults to 256 horizontal blocks.
- Discovery distance defaults to 32 horizontal blocks.
- Vertical distance does not affect reveal or discovery.
- A secret POI may set its reveal radius to zero.
- An unknown marker may appear above unexplored terrain.
- An approximate position remains fixed for that player until discovery.
- Discovery sends an action-bar message and may play a sound.
- Each player owns an independent knowledge record.
- Removing a definition suppresses its markers without deleting saved knowledge.

The server owns all state changes. A client cannot reveal or discover a POI by sending coordinates or a marker ID.

## Test structure

### Purpose

`witchercraft:poi_test_structure` is a genuine generated structure used to validate the same provider path later WitcherCraft structures will use. It is development content and must be removed or disabled before a production release.

### Initial design

- A hollow 4 by 4 by 4 stone-brick cube.
- One doorway so its orientation can be recognized.
- A visually distinct center or roof block that marks the intended POI anchor.
- Surface placement in the Overworld.
- Broad biome eligibility so it is easy to find during development.
- Frequent test placement, with final spacing chosen after inspecting the exact MCreator and Minecraft 26.1.2 worldgen format.
- A structure template, structure definition, structure set, and required tags or template pools as dictated by the exact generator.

The first batch must confirm that Minecraft records a real `StructureStart`. A placed template, feature, or procedure-built cube is not an acceptable substitute.

### MCreator ownership

Use an ordinary MCreator structure element if the installed generator supports the required structure cleanly. Its element definition and generated files remain MCreator-owned.

POI detection, persistence, packets, reload handling, and UI remain locked hand-maintained code elements in the base package. Do not put persistent POI logic in generated structure Java.

Before creating the element:

1. Inspect both pending registry directories.
2. Check whether MCreator is running.
3. Inspect the exact installed generator files instead of guessing element fields.
4. If MCreator is running, stage the registry entry rather than editing `witchercraft.mcreator`.

## Definition format

POI definitions live in a datapack directory under the `witchercraft` namespace. The exact directory name must follow the reload listener implementation and be recorded in the TDD.

The definition ID comes from the JSON resource path. A file for `witchercraft:poi_test_structure` does not repeat that ID in its contents.

A definition contains:

- Provider descriptor.
- Translation key.
- Category identifier.
- Icon texture identifier.
- Reveal radius.
- Discovery radius.
- Position uncertainty radius.
- Default filter visibility.
- Minimum visible zoom.
- Capability identifiers reserved for later systems.

Proposed logical shape:

```json
{
  "provider": {
    "type": "witchercraft:structure",
    "structure": "witchercraft:poi_test_structure"
  },
  "translation_key": "poi.witchercraft.poi_test_structure",
  "category": "witchercraft:test",
  "icon": "witchercraft:textures/gui/map/poi/poi_test_structure.png",
  "reveal_radius": 256,
  "discovery_radius": 32,
  "uncertainty_radius": 32,
  "default_visible": true,
  "minimum_zoom": 0.25,
  "capabilities": []
}
```

This is a logical contract, not permission to assume Minecraft or MCreator serialization details. Confirm identifiers and reload APIs against the installed version before implementing it.

### Validation

The reload listener rejects one invalid definition without discarding valid definitions. It logs the resource ID and reason.

Validation includes:

- Valid resource identifiers and translation keys.
- A registered provider type.
- An existing referenced structure.
- Finite, non-negative radii with hard code ceilings.
- Discovery radius no larger than reveal radius, except when reveal radius is zero for a secret POI.
- Finite minimum zoom inside the supported map zoom range.
- A bounded capability count.
- A bounded definition count.

The server loads and validates definitions. The client does not decide discovery rules from local JSON.

## Data model

### POI definition

A definition describes one kind of POI. It does not represent a generated location and is not saved in world data.

### POI instance

A provider reports an instance with:

- Stable marker ID.
- Definition ID.
- Provider type.
- Provider-specific identity.
- Dimension.
- Exact anchor X, Y, and Z.
- Active state.

The server retains Y because later signs and fast travel require a real anchor. Milestone 4 distance checks and map packets use X and Z.

### Stable structure identity

A structure marker identity derives from:

- Structure registry ID.
- Dimension ID.
- Structure start chunk.

The marker ID must be deterministic from that identity. Loading another chunk covered by the same structure must resolve to the same marker. The configured anchor must also resolve identically after a server restart.

### Player knowledge

Each player knowledge entry stores:

- Stable marker ID.
- Knowledge state, either revealed or discovered.
- Stored approximate X and Z offset for a revealed marker.
- Format version support for a later completed state.

The approximate offset is sampled uniformly inside the configured uncertainty circle when the marker is first revealed. It never changes between sessions. Discovery does not need to delete the offset, but clients receive the exact anchor afterward.

### Saved-data ownership

Use separate locked saved-data systems for:

- Shared active POI instances.
- Per-player POI knowledge.

Do not add either collection to generated `PlayerVariables`.

Saved data must survive restart, death, respawn, cloning, logout, and client cache deletion. Unknown versions and malformed logical entries must fail safely. One bad entry should not discard unrelated valid entries where the codec permits record-level recovery.

## Provider contract

A provider translates existing world objects into stable POI instances. It must never generate or force-load a chunk.

The initial provider is `witchercraft:structure`. The contract must leave room for explicit blocks, quests, entities, notice boards, bosses, and other future sources without changing the saved player-knowledge format.

The structure provider:

1. Receives an already loaded server chunk or a safe loaded-chunk observation.
2. Inspects structure starts available from that loaded state.
3. Matches starts against active POI definitions.
4. Derives the stable structure identity.
5. Resolves the configured anchor.
6. Inserts or refreshes one active shared instance.

It must not call a locate operation, add a chunk ticket, scan unloaded chunk files, or ask the world generator to calculate a missing structure start.

## Observation and discovery schedule

### Structure observation

Use a loaded-chunk event that exposes already generated structure data. `ChunkWatchEvent.Watch` is an existing candidate because the terrain system already uses it, but the exact structure API must be inspected before choosing the final hook.

Observation should be idempotent. Seeing the same start for multiple players or through multiple covered chunks must not create duplicates.

### Spatial index

Index active POI instances by dimension and coarse world cell or chunk range. Player checks query nearby cells and never iterate over the full POI registry.

The index is derived runtime state. Saved instances remain the source used to rebuild it after loading.

### Timing

- Run reveal checks every 200 server ticks.
- Run discovery checks for nearby revealed POIs every 20 server ticks.
- Offset checks across players when practical so they do not all run on the same tick.

The split preserves the requested low-frequency reveal scan while preventing a player from crossing the 32-block discovery radius between ten-second checks.

Both checks use squared horizontal distance. Neither check reads vertical distance.

## Networking

### Server authority

The client may request marker data for a map rectangle. It cannot request a reveal, discovery, exact position for an unknown marker, or arbitrary POI state mutation.

### View requests

Opening the map and moving far enough to leave the current requested area sends a bounded request containing:

- Request ID.
- Current dimension.
- Bounded X/Z rectangle or cell range.

The server responds only with entries already known to that player and inside the requested area. Requests and responses have hard size limits. Large results use bounded pages or batches followed by completion.

### Marker payload

An unknown marker receives only:

- Stable marker ID.
- Definition presentation fields safe for an unknown marker.
- Approximate X and Z.
- Unknown state.

The packet must not contain the exact position, exact coordinates hidden in another field, or enough stored provider identity to reconstruct the position.

A discovered marker may include:

- Exact X and Z.
- Translation key.
- Category.
- Icon identifier.
- Minimum zoom.
- Discovered state.

The server pushes an immediate update when proximity changes a player's knowledge. The client cache remains scoped to the current connection and world UUID.

## Map presentation

### Rendering order

Inside the clipped map viewport, draw:

1. Terrain.
2. Unknown and discovered POIs.
3. Personal waypoints and temporary target.
4. Player marker.
5. Hover card and active popup outside the terrain scissor where appropriate.

POIs use the same floating-point world-to-screen transform as terrain and waypoints.

### Marker behavior

- Unknown markers use a question-mark icon and generic hover text.
- Discovered markers use the definition icon and translated name.
- Unknown POIs disappear below their zoom threshold before discovered POIs.
- Markers outside the viewport do not render or receive hit tests.
- Overlapping marker selection chooses the nearest screen-space marker using a stable tie-breaker.
- No clustering is added in Milestone 4.

Use individual icon texture resources rather than extending the fixed waypoint atlas. This fits data-driven POI definitions and lets resource packs replace icons by identifier.

### Interaction

Left mouse input remains reserved for map panning.

- Hovering an unknown marker shows only `Undiscovered location`.
- Hovering a discovered marker shows its name, category or description, distance, and coordinates.
- Right-clicking a POI opens its details popup.
- Right-click hit priority is temporary target, personal waypoint, POI, then empty map.
- Escape closes the POI popup before closing the map.

Unknown details never expose exact coordinates or the real name.

### Filters

The existing Filters button opens a small modal overlay containing:

- Personal waypoints.
- Undiscovered POIs.
- Discovered POIs.

Preferences are client-owned and stored by the server-issued world UUID. Category filter trees wait until several real categories exist.

Any new overlay dimensions and positions belong in `MapLayout.java` and `tools/map-layout-creator.html`.

## Notifications

Discovery sends a translated action-bar message. The initial implementation may use an existing Minecraft sound while the feature remains development content.

Client preferences for disabling the message or sound belong to Milestone 5 unless adding the two boolean settings during Milestone 4 proves simpler and does not alter server behavior.

Do not add a custom sound asset solely to complete the placeholder test.

## Definition reload behavior

On datapack reload:

- Build and validate a complete replacement definition map.
- Publish it only after reload preparation succeeds.
- Keep valid definitions even when another file is invalid.
- Suppress instances whose definitions are absent.
- Retain their shared records and player knowledge.
- Reactivate retained instances if their definition returns and the provider confirms the world object still exists.
- Notify or invalidate client marker caches so stale presentation does not remain visible.

Changing reveal or discovery radii affects future checks. It does not downgrade discovered knowledge or reroll an existing approximate offset.

## Implementation batches

### Batch 4A: generated test structure

Status: implemented on 2026-09-08. File validation, resource processing, Java compilation, and dedicated-server datapack loading passed. Natural generation, `/locate`, structure-start stability, and MCreator GUI opening still require in-game or GUI validation.

1. Inspect the exact MCreator 26.1.2 structure element and generator files.
2. Create `poi_test_structure` through the correct MCreator ownership path.
3. Add its structure template and worldgen resources.
4. Give it frequent development placement in broad Overworld biomes.
5. Generate a fresh test world and confirm it appears naturally.
6. Confirm Minecraft exposes a real, stable structure start.
7. Compile and validate the element in MCreator.

Success means the cube generates naturally and its structure start remains identifiable after restart.

### Batch 4B: definitions and providers

1. Add the POI definition record and codec.
2. Add datapack reload handling and validation.
3. Add the provider contract and provider registry.
4. Implement the structure provider.
5. Add the test structure POI definition, translation, and placeholder icons.
6. Observe loaded structure starts without loading terrain.
7. Prove that one spanning structure produces one stable marker.

Success means server diagnostics report one stable POI instance for each observed test structure.

### Batch 4C: persistence and discovery

1. Add versioned shared instance saved data.
2. Add versioned per-player knowledge saved data.
3. Build the runtime spatial index.
4. Add the 200-tick reveal check.
5. Add the 20-tick discovery check.
6. Store stable uncertainty offsets.
7. Add action-bar discovery notification and the initial sound.
8. Test death, logout, restart, and two-player isolation.

Success means two players may hold different knowledge states for the same structure and those states survive restart.

### Batch 4D: networking and client cache

1. Add bounded view requests.
2. Add bounded marker data responses and completion.
3. Push immediate reveal and discovery updates.
4. Prevent exact unknown coordinates from entering payloads.
5. Scope the POI cache to the active connection and world UUID.
6. Clear or refresh cached presentation after definition reload.
7. Test malformed, oversized, repeated, and out-of-range requests.

Success means the client receives only authorized markers for the requested map area.

### Batch 4E: rendering and interaction

1. Render unknown and discovered icons through the existing transform.
2. Add hover cards and right-click details.
3. Add the three filter toggles.
4. Persist filter preferences by world UUID.
5. Apply minimum zoom and visibility rules.
6. Add required layout constants and tool preview controls.
7. Test overlap, clipping, panning, zooming, GUI scales, and aspect ratios.

Success means the test structure progresses from absent to unknown to discovered on each player's map without revealing private data.

### Batch 4F: hardening and documentation

1. Confirm map panning over unexplored terrain never creates or loads chunks.
2. Confirm structure observation does not add chunk tickets.
3. Measure tick time with many generated test structures and two players.
4. Test malformed definitions and definition removal or restoration.
5. Test duplicate observations across structure chunks and players.
6. Reconcile the master world-map plan with implemented Milestone 2 and 3 behavior.
7. Update the GDD with final player-visible POI behavior.
8. Update the TDD with ownership, identity, reload, persistence, timing, and network contracts.

Success means all Milestone 4 behavior is documented and the verification report identifies no chunk-generation path.

## Verification matrix

### Structure generation

- Generate several fresh worlds.
- Locate the test structure through normal exploration or a development-only command used solely for test setup.
- Confirm placement in expected biomes and at valid surface heights.
- Confirm the structure survives save and reload.
- Confirm the recorded start chunk and anchor remain stable.

### Discovery

- Approach from several directions.
- Confirm reveal near 256 horizontal blocks.
- Confirm discovery near 32 horizontal blocks.
- Approach while far above or below the structure and confirm Y does not matter.
- Move quickly through the discovery area and confirm the 20-tick check catches it.
- Confirm the unknown offset does not change after relogging.
- Confirm discovery replaces the approximate position with the exact position.

### Multiplayer privacy

- Use two players on a dedicated server.
- Reveal with player A while player B remains outside the radius.
- Confirm player B receives no marker.
- Reveal and discover independently in both orders.
- Inspect payload behavior to confirm unknown exact coordinates are absent.

### Persistence

- Restart the dedicated server.
- Die and respawn.
- Disconnect and reconnect.
- Delete the client cache.
- Confirm server-owned knowledge remains correct in every case.

### No forced loading

- Pan the map far beyond explored terrain.
- Request marker data for distant map areas.
- Monitor loaded chunks and tickets.
- Confirm no new structure, chunk, or terrain generation occurs.
- Confirm providers inspect only loaded chunk state.

### Reload handling

- Reload a valid definition.
- Introduce one invalid definition beside a valid one.
- Remove and restore the test definition.
- Change presentation and radii.
- Confirm saved knowledge remains and clients do not retain stale presentation.

### UI

- Test all supported GUI scales and several aspect ratios.
- Test minimum and maximum map zoom.
- Test overlapping POIs, waypoints, the temporary target, and the player marker.
- Test filter persistence between map openings and reconnects to the same world.
- Confirm another world's filter state and POI cache do not leak into the current world.

## Required file checks

For every implementation batch:

- Parse every edited JSON file.
- Check every MCreator registry entry and referenced file.
- Compile with the Gradle wrapper and Java 25.
- Inspect the diff for generated churn and unrelated changes.
- Validate changed MCreator elements in MCreator when practical.
- Report checks that still require in-game validation.

After creating or changing an MCreator element, the completion report must include an `elements touched` line with its element file, new or edited status, and MCreator browser location.

## Deferred work

- Explicit marker block provider.
- POI completion state and completion UI.
- Category filter trees.
- Quest, entity, boss, and notice-board providers.
- Fast-travel capabilities and sign interaction.
- Custom WitcherCraft discovery sound.
- Nether and other dimension POIs.
- Production structure art and placement balance.
- Removal or disabling of `poi_test_structure` before release.

## Final milestone acceptance

Milestone 4 is complete when all of the following are true:

- A real test structure generates through Minecraft worldgen.
- Loaded-chunk observation registers it once with a stable identity.
- Two players reveal and discover it independently.
- Unknown markers disclose only stable approximate positions.
- Discovery state survives death, logout, and restart.
- The map renders and filters authorized POIs correctly.
- Panning and marker requests never generate or force-load chunks.
- Malformed definitions and packets fail safely.
- GDD, TDD, and the master world-map plan match the implemented behavior.
- Remaining in-game or MCreator checks are recorded explicitly.

Implementation still requires an explicit current instruction such as `implement Batch 4A` or `go ahead with Batch 4A`.
