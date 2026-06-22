package com.political.expansion2.accessories;

/**
 * Active "while-carried" wearable abilities for the artifacts-inspired accessory line. These are
 * NOT passive stat bonuses (those live on {@link AccessoryDef2}); each constant here is driven by
 * {@link AbilityAccessories2} on the server tick and maps an original accessory onto a concrete
 * gameplay effect (flight, item magnetism, fire warding, etc.).
 *
 * <p>Mechanics are ported in spirit from the MIT-licensed Artifacts mod's ability set
 * (flight/double-jump, attractor, fire/knockback immunity, aqua-adaptation, step assist,
 * grass-grazing) but reimplemented from scratch on vanilla APIs with original names/art.
 */
public enum AccessoryAbility2 {
    /** Grants survival creative-style flight while the accessory sits in the inventory. */
    FLIGHT,
    /** Vacuums nearby dropped items and XP orbs toward the bearer. */
    MAGNET,
    /** Immunity to fire and lava damage; standing embers are snuffed instantly. */
    FIRE_WARD,
    /** Total knockback resistance — the bearer cannot be shoved. */
    ANTI_KNOCKBACK,
    /** Underwater adaptation: breathing, dolphin's grace and clear vision below the surface. */
    AQUATIC,
    /** Raises step height so the bearer walks up full blocks without jumping. */
    STEP_ASSIST,
    /** Permanent, flicker-free night vision. */
    NIGHT_SIGHT,
    /** Slowly replenishes hunger while standing on living grass. */
    GRAZING,
    /** Negates fall damage and feathers descents. */
    FEATHERFALL,
    /** Artifacts-style speed boost while sprinting on solid ground. */
    SWIFTSTRIDE
}
