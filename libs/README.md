# `libs/` — optional dev reference jars (NOT loaded automatically)

This folder is **gitignored** for `*.jar` files. It exists so developers can keep
**Minecraft 26.2 Fabric** reference mod jars locally while porting mechanics into
`politicalserver` — the same workflow as `reference/extracted/`.

## Important — standalone mod

RPG Politics 2 **does not** pull optional companion mods from Gradle or this folder at
runtime. Players need only:

- Fabric Loader
- Fabric API
- `politicalserver` (this mod)

## Do NOT drop wrong-version jars

If Fabric Loader reports that **NotEnoughAnimations**, **TRansition**, or **TRender**
require Minecraft **1.16.5**, you have **legacy 1.16.5 jars** in your instance
`mods/` folder. Remove them. Our mod ships native cast-pose animation mixins and does
**not** depend on NEA.

Optional enhancements (only if you want them, all must be **26.2 Fabric**):

| Mod | Purpose |
| --- | --- |
| JEI | Recipe categories for gear abilities / economy / relics |
| Sound Physics Remastered | Enhanced spatial audio for VFX |
| Not Enough Animations | Extra third-person animation polish (redundant with our native hooks) |

## JEI for developers

JEI is `compileOnly` in `build.gradle` (API for building the optional plugin). To test
JEI categories in dev, add a **26.2 Fabric JEI** jar to your run configuration's mod
classpath manually — it is never bundled in our release jar.

See `docs/STANDALONE.md` and `docs/integration/INTEGRATION_STATUS.md`.
