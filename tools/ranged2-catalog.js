/**
 * Source of truth for Expansion 2 ranged/magic content (arc2_ ids).
 * Consumed by gen-ranged2.js (textures/lang) and gen-ranged2-java.js (enums).
 */

const RARITIES = ['COMMON', 'UNCOMMON', 'RARE', 'EPIC', 'LEGENDARY', 'MYTHIC'];

const CASTS = [
  // Fire (7)
  { name: 'SOLAR_BEAM', display: 'Solar Beam', desc: 'Lance the sun into a single foe, igniting them with stellar fire.', mana: 32, cd: 4, power: 14, range: 28, color: 'GOLD', pattern: 'BEAM', fire: 140 },
  { name: 'INFERNO_NOVA', display: 'Inferno Nova', desc: 'Detonate a ring of hellfire around you, scorching every nearby enemy.', mana: 48, cd: 8, power: 12, range: 10, color: 'RED', pattern: 'AROUND', fire: 120 },
  { name: 'MAGMA_DOMAIN', display: 'Magma Domain', desc: 'Flood your aim point with molten earth that burns and staggers intruders.', mana: 62, cd: 12, power: 13, range: 18, color: 'GOLD', pattern: 'DOMAIN', aimR: 5.5, fire: 100, launch: true },
  { name: 'PHOENIX_BURST', display: 'Phoenix Burst', desc: 'Erupt in a frontal blaze and rise renewed with stolen life.', mana: 55, cd: 10, power: 11, range: 14, color: 'RED', pattern: 'CONE', fire: 100, healSelf: 8 },
  { name: 'CINDER_CHAIN', display: 'Cinder Chain', desc: 'Kindle a target and watch embers leap to every foe nearby.', mana: 50, cd: 9, power: 13, range: 24, color: 'GOLD', pattern: 'CHAIN', fire: 80, chainR: 5 },
  { name: 'ASH_WAVE', display: 'Ash Wave', desc: 'Sweep a choking wave of cinders across a wide frontal arc.', mana: 42, cd: 7, power: 10, range: 16, color: 'GRAY', pattern: 'CONE', fire: 60, wither: [0, 80] },
  { name: 'BLAZE_RING', display: 'Blaze Ring', desc: 'Orbit searing motes that slash everything within arm\'s reach.', mana: 38, cd: 6, power: 9, range: 9, color: 'GOLD', pattern: 'AROUND', fire: 80 },

  // Frost (7)
  { name: 'ICE_LANCE', display: 'Ice Lance', desc: 'Impale a distant target on a spear of absolute zero.', mana: 34, cd: 4, power: 15, range: 28, color: 'AQUA', pattern: 'BEAM', slow: [2, 100] },
  { name: 'GLACIAL_NOVA', display: 'Glacial Nova', desc: 'Shatter the air around you into razor ice that deep-freezes foes.', mana: 50, cd: 8, power: 12, range: 10, color: 'AQUA', pattern: 'AROUND', slow: [3, 120] },
  { name: 'PERMAFROST_DOMAIN', display: 'Permafrost Domain', desc: 'Claim ground as frozen tundra that cripples all who enter.', mana: 64, cd: 12, power: 12, range: 18, color: 'AQUA', pattern: 'DOMAIN', aimR: 6, slow: [3, 140] },
  { name: 'SHATTER_BOLT', display: 'Shatter Bolt', desc: 'Punch through armour with a bolt that explodes into ice shards.', mana: 46, cd: 7, power: 18, range: 26, color: 'AQUA', pattern: 'BEAM', slow: [1, 80] },
  { name: 'BLIZZARD_VORTEX', display: 'Blizzard Vortex', desc: 'Summon a whirling blizzard at your aim point.', mana: 66, cd: 11, power: 14, range: 20, color: 'AQUA', pattern: 'AIM', aimR: 5.5, slow: [2, 120] },
  { name: 'FROST_CHAIN', display: 'Frost Chain', desc: 'Freeze a target and let rime crackle across nearby enemies.', mana: 52, cd: 9, power: 12, range: 24, color: 'AQUA', pattern: 'CHAIN', slow: [2, 100], chainR: 5 },
  { name: 'CRYO_PULSE', display: 'Cryo Pulse', desc: 'Emit a radial frost pulse that numbs every nearby creature.', mana: 40, cd: 7, power: 10, range: 11, color: 'AQUA', pattern: 'AROUND', slow: [2, 90] },

  // Wind (6)
  { name: 'GALE_BEAM', display: 'Gale Beam', desc: 'Focus a cutting gust into a single devastating line.', mana: 28, cd: 4, power: 11, range: 26, color: 'WHITE', pattern: 'BEAM', launchFrom: true },
  { name: 'TORNADO_DOMAIN', display: 'Tornado Domain', desc: 'Anchor a cyclone at your aim point that hurls foes skyward.', mana: 58, cd: 11, power: 10, range: 18, color: 'WHITE', pattern: 'DOMAIN', aimR: 5, launch: true },
  { name: 'CYCLONE_NOVA', display: 'Cyclone Nova', desc: 'Unleash a 360° windburst that repels and shreds nearby enemies.', mana: 44, cd: 8, power: 9, range: 10, color: 'WHITE', pattern: 'AROUND', launch: true },
  { name: 'ZEPHYR_CHAIN', display: 'Zephyr Chain', desc: 'Strike one foe and ride the wind to stagger a chain of targets.', mana: 48, cd: 9, power: 10, range: 22, color: 'WHITE', pattern: 'CHAIN', chainR: 5, launchFrom: true },
  { name: 'HURRICANE_CONE', display: 'Hurricane Cone', desc: 'Channel a hurricane cone that shreds and scatters the front ranks.', mana: 42, cd: 7, power: 11, range: 15, color: 'WHITE', pattern: 'CONE', launchFrom: true },
  { name: 'MIST_VEIL', display: 'Mist Veil', desc: 'Shroud yourself in mist, blinding foes while you slip their grasp.', mana: 35, cd: 6, power: 7, range: 12, color: 'GRAY', pattern: 'AROUND', blind: 80 },

  // Storm (6)
  { name: 'THUNDER_BEAM', display: 'Thunder Beam', desc: 'Call a thunderbolt down your sightline into the first foe struck.', mana: 38, cd: 5, power: 16, range: 28, color: 'YELLOW', pattern: 'BEAM', lightning: true },
  { name: 'STORM_NOVA', display: 'Storm Nova', desc: 'Erupt with lightning in every direction around you.', mana: 54, cd: 9, power: 13, range: 11, color: 'YELLOW', pattern: 'AROUND', lightning: true },
  { name: 'VOLT_DOMAIN', display: 'Volt Domain', desc: 'Electrify an area with cascading arcs and static burns.', mana: 68, cd: 12, power: 14, range: 20, color: 'YELLOW', pattern: 'DOMAIN', aimR: 6, lightning: true },
  { name: 'STATIC_CHAIN', display: 'Static Chain', desc: 'Zap a primary target and watch voltage leap to bystanders.', mana: 52, cd: 9, power: 14, range: 26, color: 'YELLOW', pattern: 'CHAIN', chainR: 6, lightning: true },
  { name: 'EMP_BLAST', display: 'EMP Blast', desc: 'Discharge a cone of electromagnetic force that weakens machines and flesh alike.', mana: 46, cd: 8, power: 11, range: 14, color: 'YELLOW', pattern: 'CONE', weakness: [2, 120], lightning: true },
  { name: 'BALL_LIGHTNING', display: 'Ball Lightning', desc: 'Hurl an unstable orb that detonates into chained lightning.', mana: 60, cd: 10, power: 17, range: 22, color: 'YELLOW', pattern: 'AIM', aimR: 4.5, lightning: true },

  // Void (6)
  { name: 'VOID_BEAM', display: 'Void Beam', desc: 'Shear reality with a beam that withers everything it touches.', mana: 56, cd: 9, power: 18, range: 30, color: 'DARK_PURPLE', pattern: 'BEAM', wither: [1, 100] },
  { name: 'ENTROPY_NOVA', display: 'Entropy Nova', desc: 'Unmake order in a burst around you, rotting and weakening foes.', mana: 62, cd: 11, power: 13, range: 10, color: 'DARK_PURPLE', pattern: 'AROUND', wither: [2, 120], weakness: [1, 120] },
  { name: 'RIFT_DOMAIN', display: 'Rift Domain', desc: 'Tear open a void rift at your aim point that devours intruders.', mana: 72, cd: 13, power: 15, range: 18, color: 'DARK_PURPLE', pattern: 'DOMAIN', aimR: 5.5, wither: [1, 100] },
  { name: 'ABYSS_CHAIN', display: 'Abyss Chain', desc: 'Mark a victim and let the abyss hunger spread to their allies.', mana: 58, cd: 10, power: 14, range: 26, color: 'DARK_PURPLE', pattern: 'CHAIN', chainR: 5, wither: [1, 80] },
  { name: 'PHASE_LANCE', display: 'Phase Lance', desc: 'Phase through defenses to impale a lone target for massive damage.', mana: 50, cd: 8, power: 24, range: 24, color: 'DARK_PURPLE', pattern: 'BEAM' },
  { name: 'SINGULARITY', display: 'Singularity', desc: 'Collapse space at your aim point, pulling and rending everything caught.', mana: 78, cd: 14, power: 16, range: 20, color: 'DARK_PURPLE', pattern: 'PULL', aimR: 5, wither: [1, 60] },

  // Arcane (6)
  { name: 'ARCANE_BEAM', display: 'Arcane Beam', desc: 'Focus raw mana into a piercing violet beam.', mana: 36, cd: 5, power: 16, range: 28, color: 'LIGHT_PURPLE', pattern: 'BEAM' },
  { name: 'PRISMATIC_NOVA', display: 'Prismatic Nova', desc: 'Detonate a rainbow burst of unstable arcana around you.', mana: 52, cd: 9, power: 12, range: 10, color: 'LIGHT_PURPLE', pattern: 'AROUND' },
  { name: 'RUNE_DOMAIN', display: 'Rune Domain', desc: 'Inscribe a circle of runes that shreds trespassers with raw magic.', mana: 66, cd: 12, power: 13, range: 18, color: 'LIGHT_PURPLE', pattern: 'DOMAIN', aimR: 5.5 },
  { name: 'MANA_SURGE', display: 'Mana Surge', desc: 'Overcharge yourself and vent excess power in a frontal arc.', mana: 44, cd: 8, power: 11, range: 14, color: 'LIGHT_PURPLE', pattern: 'CONE', healSelf: 6 },
  { name: 'ENCHANT_CHAIN', display: 'Enchant Chain', desc: 'Tag a foe with runes that ricochet arcane damage to nearby targets.', mana: 50, cd: 9, power: 13, range: 24, color: 'LIGHT_PURPLE', pattern: 'CHAIN', chainR: 5.5 },
  { name: 'ARCANE_ECHO', display: 'Arcane Echo', desc: 'Echo a second blast at your aim point after the initial pulse.', mana: 58, cd: 10, power: 15, range: 20, color: 'LIGHT_PURPLE', pattern: 'AIM', aimR: 4.5 },

  // Shadow (6)
  { name: 'SHADOW_BEAM', display: 'Shadow Beam', desc: 'Lance shadow that withers and blinds your quarry.', mana: 48, cd: 8, power: 14, range: 28, color: 'DARK_GRAY', pattern: 'BEAM', wither: [1, 100], blind: 60 },
  { name: 'NIGHT_NOVA', display: 'Night Nova', desc: 'Expand a sphere of absolute darkness that shreds nearby foes.', mana: 56, cd: 10, power: 12, range: 10, color: 'DARK_GRAY', pattern: 'AROUND', blind: 80 },
  { name: 'UMBRA_DOMAIN', display: 'Umbra Domain', desc: 'Claim an area in living shadow that weakens and blinds intruders.', mana: 68, cd: 12, power: 12, range: 18, color: 'DARK_GRAY', pattern: 'DOMAIN', aimR: 6, blind: 100, weakness: [1, 100] },
  { name: 'SOUL_CHAIN', display: 'Soul Chain', desc: 'Drain a target and let soul hunger leap between nearby enemies.', mana: 54, cd: 9, power: 13, range: 24, color: 'DARK_AQUA', pattern: 'CHAIN', chainR: 5, drain: 2.5 },
  { name: 'PHANTOM_STRIKE', display: 'Phantom Strike', desc: 'Summon phantom blades that slash everything in a tight cone.', mana: 42, cd: 7, power: 12, range: 13, color: 'DARK_GRAY', pattern: 'CONE', wither: [0, 60] },
  { name: 'GRIM_CONE', display: 'Grim Cone', desc: 'Exhale death in a cone, withering and slowing the living.', mana: 46, cd: 8, power: 11, range: 14, color: 'DARK_GRAY', pattern: 'CONE', wither: [1, 100], slow: [1, 80] },

  // Poison (5)
  { name: 'POISON_BEAM', display: 'Poison Beam', desc: 'Inject a concentrated venom bolt into a single target.', mana: 38, cd: 5, power: 12, range: 26, color: 'DARK_GREEN', pattern: 'BEAM', poison: [2, 160] },
  { name: 'TOXIN_NOVA', display: 'Toxin Nova', desc: 'Vent toxic miasma in a ring around you.', mana: 50, cd: 8, power: 10, range: 10, color: 'DARK_GREEN', pattern: 'AROUND', poison: [2, 140] },
  { name: 'PLAGUE_DOMAIN', display: 'Plague Domain', desc: 'Seed a plague zone that poisons and withers trespassers.', mana: 64, cd: 12, power: 11, range: 18, color: 'DARK_GREEN', pattern: 'DOMAIN', aimR: 5.5, poison: [2, 160], wither: [0, 80] },
  { name: 'VENOM_CHAIN', display: 'Venom Chain', desc: 'Envenom a target and spread the toxin to nearby foes.', mana: 48, cd: 9, power: 11, range: 22, color: 'DARK_GREEN', pattern: 'CHAIN', chainR: 5, poison: [2, 120] },
  { name: 'ACID_SPRAY', display: 'Acid Spray', desc: 'Spray corrosive acid across a wide frontal cone.', mana: 40, cd: 7, power: 10, range: 12, color: 'GREEN', pattern: 'CONE', poison: [1, 100], weakness: [1, 80] },

  // Holy (5)
  { name: 'RADIANT_BEAM', display: 'Radiant Beam', desc: 'Smite a distant foe with a beam of consecrated light.', mana: 42, cd: 6, power: 14, range: 28, color: 'YELLOW', pattern: 'BEAM', blind: 40 },
  { name: 'BLESSING_NOVA', display: 'Blessing Nova', desc: 'Erupt with holy light that mends allies and scorches undead nearby.', mana: 58, cd: 10, power: 10, range: 11, color: 'YELLOW', pattern: 'HOLY_NOVA', healSelf: 8, healAllies: 8 },
  { name: 'SANCTUM_DOMAIN', display: 'Sanctum Domain', desc: 'Bless ground so allies within regenerate while foes are smitten.', mana: 66, cd: 12, power: 10, range: 16, color: 'YELLOW', pattern: 'SANCTUM', aimR: 6, healAllies: 6 },
  { name: 'DIVINE_CHAIN', display: 'Divine Chain', desc: 'Chain holy judgment between clustered enemies.', mana: 52, cd: 9, power: 13, range: 24, color: 'YELLOW', pattern: 'CHAIN', chainR: 5.5 },
  { name: 'DIVINE_WRATH', display: 'Divine Wrath', desc: 'Unleash a cone of searing judgment from on high.', mana: 48, cd: 8, power: 13, range: 15, color: 'YELLOW', pattern: 'CONE', blind: 60 },

  // Necro (5)
  { name: 'NECROTIC_BEAM', display: 'Necrotic Beam', desc: 'Rot a single target with concentrated necrotic force.', mana: 46, cd: 7, power: 14, range: 26, color: 'DARK_GREEN', pattern: 'BEAM', wither: [2, 120] },
  { name: 'ROT_NOVA', display: 'Rot Nova', desc: 'Pulse decay in every direction, withering the living.', mana: 56, cd: 10, power: 12, range: 10, color: 'DARK_GREEN', pattern: 'AROUND', wither: [2, 140], weakness: [1, 100] },
  { name: 'GRAVE_DOMAIN', display: 'Grave Domain', desc: 'Open graves at your aim point that weaken and rot intruders.', mana: 68, cd: 12, power: 12, range: 18, color: 'DARK_GRAY', pattern: 'DOMAIN', aimR: 5.5, wither: [1, 120], weakness: [2, 120] },
  { name: 'BONE_CHAIN', display: 'Bone Chain', desc: 'Skull shards leap from victim to victim along a necrotic chain.', mana: 52, cd: 9, power: 12, range: 22, color: 'GRAY', pattern: 'CHAIN', chainR: 5, wither: [1, 100] },
  { name: 'SKULL_BURST', display: 'Skull Burst', desc: 'Detonate a cluster of screaming skulls at your aim point.', mana: 60, cd: 10, power: 15, range: 20, color: 'DARK_GRAY', pattern: 'AIM', aimR: 4.5, wither: [1, 80] },

  // Blood (4)
  { name: 'BLOOD_BEAM', display: 'Blood Beam', desc: 'Lance a crimson beam that returns life to you from the wound.', mana: 44, cd: 7, power: 14, range: 26, color: 'RED', pattern: 'BEAM', drain: 3 },
  { name: 'CRIMSON_NOVA', display: 'Crimson Nova', desc: 'Explode in a blood nova, stealing vitality from nearby foes.', mana: 54, cd: 9, power: 11, range: 10, color: 'RED', pattern: 'AROUND', drain: 2 },
  { name: 'VAMPIRE_CHAIN', display: 'Vampire Chain', desc: 'Drain a target and siphon through a chain of victims.', mana: 56, cd: 10, power: 12, range: 22, color: 'RED', pattern: 'CHAIN', chainR: 5, drain: 2.5 },
  { name: 'SANGUINE_RITE', display: 'Sanguine Rite', desc: 'Sacrifice your blood to empower a devastating frontal slash.', mana: 38, cd: 6, power: 18, range: 12, color: 'RED', pattern: 'CONE', selfCost: 4 },

  // Nature (5)
  { name: 'NATURE_BEAM', display: 'Nature Beam', desc: 'Fire a thorn-laced beam that poisons and slows.', mana: 36, cd: 5, power: 12, range: 26, color: 'GREEN', pattern: 'BEAM', poison: [1, 100], slow: [1, 60] },
  { name: 'VERDANT_NOVA', display: 'Verdant Nova', desc: 'Bloom life around you, healing allies while thorns punish foes.', mana: 56, cd: 10, power: 9, range: 11, color: 'GREEN', pattern: 'HOLY_NOVA', healSelf: 10, healAllies: 10 },
  { name: 'GROVE_DOMAIN', display: 'Grove Domain', desc: 'Root an area in living wood that entangles and poisons enemies.', mana: 62, cd: 11, power: 10, range: 18, color: 'GREEN', pattern: 'DOMAIN', aimR: 5.5, slow: [2, 100], poison: [1, 80] },
  { name: 'THORN_CHAIN', display: 'Thorn Chain', desc: 'Pierce a foe and let thorns whip to every nearby target.', mana: 48, cd: 9, power: 11, range: 22, color: 'GREEN', pattern: 'CHAIN', chainR: 5, poison: [1, 80] },
  { name: 'WOLF_SUMMON', display: 'Spirit Pack', desc: 'Call spectral wolves to maul foes near your aim point.', mana: 70, cd: 14, power: 8, range: 18, color: 'GREEN', pattern: 'SUMMON', summon: 'WOLF', count: 3 },

  // Chakram (3)
  { name: 'CHAKRAM_VOLLEY', display: 'Chakram Volley', desc: 'Loose a fan of spinning chakrams down your forward arc.', mana: 32, cd: 5, power: 10, range: 18, color: 'WHITE', pattern: 'CONE' },
  { name: 'CHAKRAM_ORBIT', display: 'Chakram Orbit', desc: 'Orbit razor discs that cut everything nearby.', mana: 38, cd: 6, power: 9, range: 9, color: 'WHITE', pattern: 'AROUND' },
  { name: 'RICOCHET_STORM', display: 'Ricochet Storm', desc: 'Unleash chakrams that ricochet wildly through nearby ranks.', mana: 44, cd: 7, power: 11, range: 12, color: 'WHITE', pattern: 'AROUND' },

  // Gun-as-staff (3)
  { name: 'BOLT_SNIPER', display: 'Arc Bolt', desc: 'Fire a mana-charged sniper bolt through the first target aligned.', mana: 42, cd: 6, power: 22, range: 32, color: 'AQUA', pattern: 'BEAM' },
  { name: 'SHOTGUN_SPRAY', display: 'Scatter Shot', desc: 'Vent a short-range cone of arcane buckshot.', mana: 36, cd: 5, power: 9, range: 10, color: 'GRAY', pattern: 'CONE' },
  { name: 'PLASMA_LANCE', display: 'Plasma Lance', desc: 'Discharge a sustained plasma lance at extreme range.', mana: 52, cd: 8, power: 20, range: 30, color: 'LIGHT_PURPLE', pattern: 'BEAM', lightning: true },

  // Cosmic (5)
  { name: 'STAR_BEAM', display: 'Star Beam', desc: 'Draw starfire into a beam that pierces the heavens.', mana: 50, cd: 8, power: 17, range: 30, color: 'LIGHT_PURPLE', pattern: 'BEAM' },
  { name: 'COSMIC_NOVA', display: 'Cosmic Nova', desc: 'Expand a supernova burst around you.', mana: 64, cd: 11, power: 14, range: 11, color: 'LIGHT_PURPLE', pattern: 'AROUND' },
  { name: 'METEOR_SHOWER', display: 'Meteor Shower', desc: 'Rain meteors across a wide zone at your aim point.', mana: 74, cd: 13, power: 16, range: 22, color: 'RED', pattern: 'AIM', aimR: 6, fire: 100, launch: true },
  { name: 'ECLIPSE_DOMAIN', display: 'Eclipse Domain', desc: 'Cast an eclipse that blinds foes and empowers your strikes in the zone.', mana: 70, cd: 12, power: 13, range: 18, color: 'DARK_PURPLE', pattern: 'DOMAIN', aimR: 6, blind: 80 },
  { name: 'ASTRAL_CHAIN', display: 'Astral Chain', desc: 'Link enemies with astral threads that detonate in sequence.', mana: 58, cd: 10, power: 14, range: 26, color: 'LIGHT_PURPLE', pattern: 'CHAIN', chainR: 6 },

  // Time (3)
  { name: 'CHRONO_BEAM', display: 'Chrono Beam', desc: 'Strike a foe and lock them in temporal stasis.', mana: 48, cd: 8, power: 14, range: 26, color: 'AQUA', pattern: 'BEAM', slow: [4, 80] },
  { name: 'TIME_WARP', display: 'Time Warp', desc: 'Warp time in a zone: haste allies, slow enemies.', mana: 62, cd: 11, power: 8, range: 16, color: 'AQUA', pattern: 'SANCTUM', aimR: 5.5, slow: [2, 100] },
  { name: 'STASIS_BOLT', display: 'Stasis Bolt', desc: 'Fire a bolt that nearly freezes a single target in place.', mana: 44, cd: 7, power: 16, range: 28, color: 'AQUA', pattern: 'BEAM', slow: [5, 60] },

  // Crystal (3)
  { name: 'CRYSTAL_BEAM', display: 'Crystal Beam', desc: 'Focus gem-light into a piercing crystal lance.', mana: 40, cd: 6, power: 15, range: 28, color: 'AQUA', pattern: 'BEAM' },
  { name: 'GEM_NOVA', display: 'Gem Nova', desc: 'Shatter outward in a nova of razor crystal shards.', mana: 52, cd: 9, power: 12, range: 10, color: 'AQUA', pattern: 'AROUND' },
  { name: 'SHARD_DOMAIN', display: 'Shard Domain', desc: 'Seed an area with erupting crystal spikes.', mana: 64, cd: 11, power: 13, range: 18, color: 'AQUA', pattern: 'DOMAIN', aimR: 5.5 },

  // Extra summons / utility (3) — pushes past 60 unique
  { name: 'GOLEM_SUMMON', display: 'Iron Aegis', desc: 'Summon a loyal iron golem at your aim point.', mana: 85, cd: 16, power: 0, range: 16, color: 'GRAY', pattern: 'SUMMON', summon: 'GOLEM', count: 1 },
  { name: 'VEX_SWARM', display: 'Vex Swarm', desc: 'Unleash a trio of vex shades on your target area.', mana: 72, cd: 14, power: 6, range: 18, color: 'LIGHT_PURPLE', pattern: 'SUMMON', summon: 'VEX', count: 3 },
  { name: 'MEND_BEAM', display: 'Mend Beam', desc: 'Channel restorative light to yourself and allies ahead.', mana: 42, cd: 7, power: 0, range: 16, color: 'GREEN', pattern: 'HEAL', healSelf: 10, healAllies: 10 },
];

function statTier(rarity, slot) {
  const base = { COMMON: 1, UNCOMMON: 1.4, RARE: 1.9, EPIC: 2.5, LEGENDARY: 3.2, MYTHIC: 4.2 }[rarity];
  const intBase = { bow: 55, crossbow: 75, chakram: 65, gun: 85, wand: 95, staff: 130, tome: 150, orb: 120, focus: 140 }[slot];
  const dmgBase = { bow: 70, crossbow: 85, chakram: 72, gun: 95, wand: 48, staff: 72, tome: 58, orb: 52, focus: 65 }[slot];
  const roll = (b, m = 1) => Math.round(b * base * m);
  return {
    int: roll(intBase, 1.05),
    dmg: roll(dmgBase),
    str: roll(dmgBase * 0.55),
    cc: Math.min(35, Math.round(10 + base * 4)),
    cd: Math.min(95, Math.round(25 + base * 12)),
    fer: rarity === 'MYTHIC' ? 8 : rarity === 'LEGENDARY' ? 5 : rarity === 'EPIC' ? 3 : rarity === 'RARE' ? 1 : 0,
  };
}

function w(name, display, cat, rarity, cast, tex, tint) {
  const s = statTier(rarity, cat);
  return { name, display, cat, rarity, cast, tex, tint, ...s };
}

const WEAPONS = [
  // Bows (12)
  w('SOLAR_BOW', 'Helios Longbow', 'bow', 'COMMON', 'SOLAR_BEAM', 'bow', 'ember'),
  w('CINDER_BOW', 'Cinder Recurve', 'bow', 'UNCOMMON', 'CINDER_CHAIN', 'bow', 'ember'),
  w('INFERNO_BOW', 'Inferno Greatbow', 'bow', 'RARE', 'INFERNO_NOVA', 'bow', 'crimson'),
  w('MAGMA_BOW', 'Magma Strider Bow', 'bow', 'RARE', 'MAGMA_DOMAIN', 'bow', 'ember'),
  w('PHOENIX_BOW', 'Phoenix Wing Bow', 'bow', 'EPIC', 'PHOENIX_BURST', 'bow', 'gold'),
  w('ASH_BOW', 'Ashwind Bow', 'bow', 'EPIC', 'ASH_WAVE', 'bow', 'shadow'),
  w('BLAZE_BOW', 'Blazering Bow', 'bow', 'LEGENDARY', 'BLAZE_RING', 'bow', 'ember'),
  w('GLACIAL_BOW', 'Glacial Moonbow', 'bow', 'RARE', 'ICE_LANCE', 'bow', 'frost'),
  w('BLIZZARD_BOW', 'Blizzard Hunter Bow', 'bow', 'EPIC', 'BLIZZARD_VORTEX', 'bow', 'frost'),
  w('PERMA_BOW', 'Permafrost Warbow', 'bow', 'LEGENDARY', 'PERMAFROST_DOMAIN', 'bow', 'frost'),
  w('STORM_BOW', 'Tempest Arc Bow', 'bow', 'LEGENDARY', 'STATIC_CHAIN', 'bow', 'thunder'),
  w('VOID_BOW', 'Voidstring Bow', 'bow', 'MYTHIC', 'VOID_BEAM', 'bow', 'void'),

  // Crossbows (10)
  w('SNIPER_XBOW', 'Arcane Sniper', 'crossbow', 'UNCOMMON', 'BOLT_SNIPER', 'crossbow', 'steel'),
  w('VENOM_XBOW', 'Viper Crossbow', 'crossbow', 'RARE', 'POISON_BEAM', 'crossbow', 'poison'),
  w('FROST_XBOW', 'Rime Arbalest', 'crossbow', 'RARE', 'SHATTER_BOLT', 'crossbow', 'frost'),
  w('THUNDER_XBOW', 'Voltaic Arbalest', 'crossbow', 'EPIC', 'BALL_LIGHTNING', 'crossbow', 'thunder'),
  w('SHADOW_XBOW', 'Nightbolt Crossbow', 'crossbow', 'EPIC', 'SHADOW_BEAM', 'crossbow', 'shadow'),
  w('HOLY_XBOW', 'Radiant Crossbow', 'crossbow', 'LEGENDARY', 'RADIANT_BEAM', 'crossbow', 'holy'),
  w('NECRO_XBOW', 'Gravebolt Crossbow', 'crossbow', 'LEGENDARY', 'NECROTIC_BEAM', 'crossbow', 'necro'),
  w('PLASMA_XBOW', 'Plasma Caster', 'crossbow', 'EPIC', 'PLASMA_LANCE', 'crossbow', 'arcane'),
  w('COSMIC_XBOW', 'Starfall Crossbow', 'crossbow', 'MYTHIC', 'ASTRAL_CHAIN', 'crossbow', 'void'),
  w('CHRONO_XBOW', 'Chrono Arbalest', 'crossbow', 'LEGENDARY', 'STASIS_BOLT', 'crossbow', 'aqua'),

  // Chakrams (8)
  w('SOLAR_CHAKRAM', 'Sun Disc', 'chakram', 'UNCOMMON', 'CHAKRAM_VOLLEY', 'chakram', 'gold'),
  w('FROST_CHAKRAM', 'Rime Ring', 'chakram', 'RARE', 'CHAKRAM_ORBIT', 'chakram', 'frost'),
  w('STORM_CHAKRAM', 'Thunder Ring', 'chakram', 'RARE', 'RICOCHET_STORM', 'chakram', 'thunder'),
  w('SHADOW_CHAKRAM', 'Umbra Chakram', 'chakram', 'EPIC', 'PHANTOM_STRIKE', 'chakram', 'shadow'),
  w('VENOM_CHAKRAM', 'Toxin Wheel', 'chakram', 'EPIC', 'VENOM_CHAIN', 'chakram', 'poison'),
  w('BLOOD_CHAKRAM', 'Crimson Slicer', 'chakram', 'LEGENDARY', 'VAMPIRE_CHAIN', 'chakram', 'blood'),
  w('ARCANE_CHAKRAM', 'Rune Chakram', 'chakram', 'LEGENDARY', 'ENCHANT_CHAIN', 'chakram', 'arcane'),
  w('VOID_CHAKRAM', 'Abyss Ring', 'chakram', 'MYTHIC', 'ABYSS_CHAIN', 'chakram', 'void'),

  // Guns-as-staves (10)
  w('EMBER_GUN', 'Emberlock Pistol', 'gun', 'COMMON', 'SHOTGUN_SPRAY', 'gun', 'ember'),
  w('VOLT_GUN', 'Volt Rifle', 'gun', 'UNCOMMON', 'BOLT_SNIPER', 'gun', 'thunder'),
  w('FROST_GUN', 'Cryo Carbine', 'gun', 'RARE', 'ICE_LANCE', 'gun', 'frost'),
  w('TOXIN_GUN', 'Chem Spitter', 'gun', 'RARE', 'ACID_SPRAY', 'gun', 'poison'),
  w('VOID_GUN', 'Null Cannon', 'gun', 'EPIC', 'PHASE_LANCE', 'gun', 'void'),
  w('ARC_GUN', 'Arc Repeater', 'gun', 'EPIC', 'PLASMA_LANCE', 'gun', 'arcane'),
  w('SOUL_GUN', 'Soul Harpoon Gun', 'gun', 'LEGENDARY', 'SOUL_CHAIN', 'gun', 'soul'),
  w('STAR_GUN', 'Nova Blaster', 'gun', 'LEGENDARY', 'COSMIC_NOVA', 'gun', 'arcane'),
  w('EMP_GUN', 'EMP Launcher', 'gun', 'EPIC', 'EMP_BLAST', 'gun', 'thunder'),
  w('METEOR_GUN', 'Meteor Launcher', 'gun', 'MYTHIC', 'METEOR_SHOWER', 'gun', 'ember'),

  // Wands (12)
  w('SPARK_WAND', 'Spark Wand', 'wand', 'COMMON', 'ARCANE_BEAM', 'wand', 'arcane'),
  w('GALE_WAND', 'Gale Wand', 'wand', 'UNCOMMON', 'GALE_BEAM', 'wand', 'wind'),
  w('FLAME_WAND', 'Flame Wand', 'wand', 'UNCOMMON', 'SOLAR_BEAM', 'wand', 'ember'),
  w('RIME_WAND', 'Rime Wand', 'wand', 'RARE', 'CRYO_PULSE', 'wand', 'frost'),
  w('VENOM_WAND', 'Venom Wand', 'wand', 'RARE', 'POISON_BEAM', 'wand', 'poison'),
  w('MEND_WAND', 'Mend Wand', 'wand', 'RARE', 'MEND_BEAM', 'wand', 'emerald'),
  w('BLOOD_WAND', 'Sanguine Wand', 'wand', 'EPIC', 'BLOOD_BEAM', 'wand', 'blood'),
  w('THUNDER_WAND', 'Storm Wand', 'wand', 'EPIC', 'THUNDER_BEAM', 'wand', 'thunder'),
  w('SHADOW_WAND', 'Shadow Wand', 'wand', 'EPIC', 'SHADOW_BEAM', 'wand', 'shadow'),
  w('TIME_WAND', 'Chrono Wand', 'wand', 'LEGENDARY', 'CHRONO_BEAM', 'wand', 'aqua'),
  w('STAR_WAND', 'Star Wand', 'wand', 'LEGENDARY', 'STAR_BEAM', 'wand', 'arcane'),
  w('VOID_WAND', 'Void Wand', 'wand', 'MYTHIC', 'SINGULARITY', 'wand', 'void'),

  // Staves (12)
  w('PYRO_STAFF', 'Pyre Staff', 'staff', 'RARE', 'MAGMA_DOMAIN', 'staff', 'ember'),
  w('CRYO_STAFF', 'Cryo Monarch Staff', 'staff', 'RARE', 'GLACIAL_NOVA', 'staff', 'frost'),
  w('GALE_STAFF', 'Tempest Staff', 'staff', 'EPIC', 'TORNADO_DOMAIN', 'staff', 'wind'),
  w('STORM_STAFF', 'Stormcaller Staff', 'staff', 'EPIC', 'VOLT_DOMAIN', 'staff', 'thunder'),
  w('LIFE_STAFF', 'Verdant Staff', 'staff', 'LEGENDARY', 'VERDANT_NOVA', 'staff', 'emerald'),
  w('HOLY_STAFF', 'Sanctified Staff', 'staff', 'LEGENDARY', 'BLESSING_NOVA', 'staff', 'holy'),
  w('NECRO_STAFF', 'Gravecaller Staff', 'staff', 'EPIC', 'ROT_NOVA', 'staff', 'necro'),
  w('BLOOD_STAFF', 'Hemomancer Staff', 'staff', 'EPIC', 'CRIMSON_NOVA', 'staff', 'blood'),
  w('NATURE_STAFF', 'Grovekeeper Staff', 'staff', 'LEGENDARY', 'GROVE_DOMAIN', 'staff', 'emerald'),
  w('COSMIC_STAFF', 'Cosmos Staff', 'staff', 'MYTHIC', 'ECLIPSE_DOMAIN', 'staff', 'void'),
  w('GOLEM_STAFF', 'Aegis Staff', 'staff', 'LEGENDARY', 'GOLEM_SUMMON', 'staff', 'iron'),
  w('VEX_STAFF', 'Hexweaver Staff', 'staff', 'MYTHIC', 'VEX_SWARM', 'staff', 'cursed'),

  // Tomes (12)
  w('FIRE_TOME', 'Tome of Embers', 'tome', 'RARE', 'INFERNO_NOVA', 'book', 'crimson'),
  w('FROST_TOME', 'Tome of Rime', 'tome', 'RARE', 'PERMAFROST_DOMAIN', 'book', 'frost'),
  w('GALE_TOME', 'Tome of Gales', 'tome', 'EPIC', 'HURRICANE_CONE', 'book', 'wind'),
  w('STORM_TOME', 'Tome of Thunder', 'tome', 'EPIC', 'STORM_NOVA', 'book', 'thunder'),
  w('ARCANE_TOME', 'Tome of Runes', 'tome', 'RARE', 'RUNE_DOMAIN', 'book', 'arcane'),
  w('SHADOW_TOME', 'Tome of Night', 'tome', 'EPIC', 'UMBRA_DOMAIN', 'book', 'shadow'),
  w('POISON_TOME', 'Tome of Plagues', 'tome', 'EPIC', 'PLAGUE_DOMAIN', 'book', 'poison'),
  w('HOLY_TOME', 'Tome of Light', 'tome', 'LEGENDARY', 'SANCTUM_DOMAIN', 'book', 'holy'),
  w('NECRO_TOME', 'Tome of Graves', 'tome', 'LEGENDARY', 'GRAVE_DOMAIN', 'book', 'necro'),
  w('BLOOD_TOME', 'Tome of Sanguis', 'tome', 'LEGENDARY', 'SANGUINE_RITE', 'book', 'blood'),
  w('COSMIC_TOME', 'Tome of Stars', 'tome', 'MYTHIC', 'METEOR_SHOWER', 'book', 'arcane'),
  w('TIME_TOME', 'Tome of Hours', 'tome', 'LEGENDARY', 'TIME_WARP', 'book', 'aqua'),

  // Orbs (8)
  w('ARCANE_ORB', 'Prism Orb', 'orb', 'RARE', 'PRISMATIC_NOVA', 'orb', 'arcane'),
  w('FROST_ORB', 'Glacier Orb', 'orb', 'RARE', 'FROST_CHAIN', 'orb', 'frost'),
  w('STORM_ORB', 'Stormheart Orb', 'orb', 'EPIC', 'BALL_LIGHTNING', 'orb', 'thunder'),
  w('VOID_ORB', 'Voidheart Orb', 'orb', 'EPIC', 'ENTROPY_NOVA', 'orb', 'void'),
  w('LIFE_ORB', 'Lifeheart Orb', 'orb', 'LEGENDARY', 'MEND_BEAM', 'orb', 'emerald'),
  w('BLOOD_ORB', 'Crimson Orb', 'orb', 'LEGENDARY', 'CRIMSON_NOVA', 'orb', 'blood'),
  w('CRYSTAL_ORB', 'Crystal Orb', 'orb', 'EPIC', 'GEM_NOVA', 'orb', 'aqua'),
  w('COSMIC_ORB', 'Nebula Orb', 'orb', 'MYTHIC', 'COSMIC_NOVA', 'orb', 'void'),

  // Domain focus items (8)
  w('EMBER_FOCUS', 'Ember Domain Focus', 'focus', 'EPIC', 'MAGMA_DOMAIN', 'focus', 'ember'),
  w('FROST_FOCUS', 'Frost Domain Focus', 'focus', 'EPIC', 'PERMAFROST_DOMAIN', 'focus', 'frost'),
  w('STORM_FOCUS', 'Storm Domain Focus', 'focus', 'LEGENDARY', 'VOLT_DOMAIN', 'focus', 'thunder'),
  w('VOID_FOCUS', 'Void Domain Focus', 'focus', 'LEGENDARY', 'RIFT_DOMAIN', 'focus', 'void'),
  w('GROVE_FOCUS', 'Grove Domain Focus', 'focus', 'LEGENDARY', 'GROVE_DOMAIN', 'focus', 'emerald'),
  w('PLAGUE_FOCUS', 'Plague Domain Focus', 'focus', 'EPIC', 'PLAGUE_DOMAIN', 'focus', 'poison'),
  w('SANCTUM_FOCUS', 'Sanctum Domain Focus', 'focus', 'LEGENDARY', 'SANCTUM_DOMAIN', 'focus', 'holy'),
  w('ECLIPSE_FOCUS', 'Eclipse Domain Focus', 'focus', 'MYTHIC', 'ECLIPSE_DOMAIN', 'focus', 'void'),
];

module.exports = { CASTS, WEAPONS, RARITIES };
