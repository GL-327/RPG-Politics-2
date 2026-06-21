/**
 * Common (both-sides) and server-side mixins for RPG Politics 2.
 *
 * <p>Mixins here are referenced from {@code politicalserver.mixins.json} (the
 * {@code mixins} / {@code server} arrays) and wired via {@code fabric.mod.json}.
 * This project targets the <b>non-remapping Loom</b> (MC 26.2 ships deobfuscated
 * with Mojang names), so mixin targets use official names directly and no refmap
 * lookup is required at runtime.
 *
 * <h2>Policy (see docs/integration/PLAN.md §Architecture)</h2>
 * Default to Fabric API events / clean overrides. Add a mixin <i>only</i> when it
 * genuinely unlocks capability that events cannot (deep damage/attribute math,
 * targets with no callback). Keep each mixin narrow, single-purpose, and
 * documented. Prefer {@code @Inject} with {@code cancellable}/{@code @ModifyVariable}
 * over {@code @Overwrite}. Client render/visibility/animation/VFX mixins live in
 * {@code com.political.client.mixin} instead.
 */
package com.political.mixin;
