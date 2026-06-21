/**
 * Client-only mixins for RPG Politics 2 (render, visibility, animation, input,
 * HUD/VFX hooks).
 *
 * <p>Referenced from {@code politicalserver.client.mixins.json} (the
 * {@code client} array), which is registered in {@code fabric.mod.json} with
 * {@code "environment": "client"}. Non-remapping Loom: use official Mojang names,
 * no refmap required.
 *
 * <h2>Typical use here</h2>
 * <ul>
 *   <li>Render hooks for cursed-energy auras / domain overlays / world-space VFX
 *       that have no Fabric callback.</li>
 *   <li>Animation-rig hooks (complementing Not Enough Animations) for techniques.</li>
 *   <li>Input handling for technique binding/casting where a clean keybind event
 *       is insufficient.</li>
 * </ul>
 * Keep mixins narrow and documented; prefer events/overrides when they suffice
 * (e.g. curse visibility is handled by {@code Entity#isInvisibleTo(Player)}).
 */
package com.political.client.mixin;
