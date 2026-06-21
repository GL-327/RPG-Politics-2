/**
 * Generates SpiritSpecies2.java from spirit2-species-data.js.
 * Usage: node tools/gen-spirit2-enum.js
 */
const fs = require('fs');
const path = require('path');
const SPECIES = require('./spirit2-species-data');

const OUT = path.join(__dirname, '..', 'src', 'main', 'java', 'com', 'political', 'expansion2', 'curses', 'SpiritSpecies2.java');

function toEnumName(id) {
  return id.replace(/^spirit2_/, '').toUpperCase();
}

function behSet(beh) {
  return 'EnumSet.of(' + beh.map(b => 'Behavior2.' + b).join(', ') + ')';
}

const lines = SPECIES.map((sp, i) => {
  const enumName = toEnumName(sp.id);
  const term = i === SPECIES.length - 1 ? ');' : '),';
  return `    ${enumName}("${sp.id}", "${sp.name.replace(/"/g, '\\"')}", ${sp.band}, ${sp.hp}, ${sp.dmg}, ${sp.spd}, ${sp.scale}f, ${sp.kb},\n` +
    `            ModelKind.${sp.model}, ${sp.w}f, ${sp.h}f, ChatFormatting.${sp.color}, ${sp.loot}, ${sp.boss},\n` +
    `            ${behSet(sp.beh)}${term}`;
});

const java = `package com.political.expansion2.curses;

import net.minecraft.ChatFormatting;

import java.util.EnumSet;
import java.util.Set;

/**
 * Phase-2 cursed spirit roster ({@value #COUNT} species). Entity ids are prefixed {@code spirit2_}.
 * Generated from tools/spirit2-species-data.js — edit the data file and re-run {@code gen-spirit2-enum.js}.
 */
public enum SpiritSpecies2 {

${lines.join('\n\n')}

    /** Client model archetype (humanoid rig variants). */
    public enum ModelKind { GAUNT, SWARM_TINY, HULKING, HORNED, WINGED, SERPENT, TOOL, CORPSE }

    public static final int COUNT = ${SPECIES.length};

    private final String id;
    private final String displayName;
    private final int gradeBand;
    private final double baseHealth;
    private final double baseDamage;
    private final double moveSpeed;
    private final float baseScale;
    private final double knockbackResist;
    private final ModelKind modelKind;
    private final float hitboxWidth;
    private final float hitboxHeight;
    private final ChatFormatting nameColor;
    private final int lootTier;
    private final boolean boss;
    private final Set<Behavior2> behaviors;

    SpiritSpecies2(String id, String displayName, int gradeBand, double baseHealth, double baseDamage,
                   double moveSpeed, float baseScale, double knockbackResist, ModelKind modelKind,
                   float hitboxWidth, float hitboxHeight, ChatFormatting nameColor, int lootTier,
                   boolean boss, Set<Behavior2> behaviors) {
        this.id = id;
        this.displayName = displayName;
        this.gradeBand = gradeBand;
        this.baseHealth = baseHealth;
        this.baseDamage = baseDamage;
        this.moveSpeed = moveSpeed;
        this.baseScale = baseScale;
        this.knockbackResist = knockbackResist;
        this.modelKind = modelKind;
        this.hitboxWidth = hitboxWidth;
        this.hitboxHeight = hitboxHeight;
        this.nameColor = nameColor;
        this.lootTier = lootTier;
        this.boss = boss;
        this.behaviors = behaviors;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public int gradeBand() { return gradeBand; }
    public double baseHealth() { return baseHealth; }
    public double baseDamage() { return baseDamage; }
    public double moveSpeed() { return moveSpeed; }
    public float baseScale() { return baseScale; }
    public double knockbackResist() { return knockbackResist; }
    public ModelKind modelKind() { return modelKind; }
    public float hitboxWidth() { return hitboxWidth; }
    public float hitboxHeight() { return hitboxHeight; }
    public ChatFormatting nameColor() { return nameColor; }
    public int lootTier() { return lootTier; }
    public boolean boss() { return boss; }

    public boolean has(Behavior2 b) { return behaviors.contains(b); }

    public static SpiritSpecies2 byId(String id) {
        for (SpiritSpecies2 s : values()) if (s.id.equalsIgnoreCase(id)) return s;
        return null;
    }
}
`;

fs.mkdirSync(path.dirname(OUT), { recursive: true });
fs.writeFileSync(OUT, java);
console.log('SpiritSpecies2.java written:', SPECIES.length, 'species ->', OUT);
