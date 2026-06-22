# Standalone Mod — RPG Politics 2

RPG Politics 2 is a **standalone Fabric mod**. Players need only:

1. **Fabric Loader** (0.19+)
2. **Fabric API** (0.152+ for MC 26.2)
3. **`politicalserver`** (this mod)

No companion mods are required for JJK techniques, viltrumite flight, accessories, worldgen biomes, VFX, or politics systems.

## What changed (vs external-deps approach)

Reference mod mechanics (JujutsuCraft, cursedfate, viltrumitecore, Artifacts, Terralith, etc.) are **ported into our own Java and datapack resources** under `com.political.*` and `data/politicalserver/`. We do **not** ship or require those mod JARs.

Gradle **no longer** pulls NotEnoughAnimations, Sound Physics Remastered, or JEI at dev runtime. JEI integration is `compileOnly` — the `@JeiPlugin` class loads only when the player installs JEI separately.

## Fixing the 1.16.5 NEA / TRansition / TRender crash

If Fabric Loader reports:

> NotEnoughAnimations 1.12.4 requires minecraft 1.16.5  
> TRansition / TRender require minecraft 1.16.5

You have **legacy 1.16.5 animation jars** in your instance `mods/` folder (or an old dev classpath). **Remove them.**

Our mod includes **native cast-pose animation** via `HumanoidModelCastPoseMixin` — NEA is optional polish only (must be a **26.2 Fabric** build if installed at all).

## Optional enhancements (`suggests` in fabric.mod.json)

| Mod | Role |
| --- | --- |
| **JEI** | Browse gear-ability / economy / relic recipe categories |
| **Sound Physics Remastered** | Spatial reverb on world sounds (VFX already use correct categories natively) |
| **Not Enough Animations** | Extra third-person animation layers (redundant with native cast poses) |

## Developer notes

- **Build:** `.\gradlew.bat clean build`
- **JEI dev testing:** add a 26.2 Fabric JEI jar to your run config manually; it is not on the Gradle runtime classpath.
- **Reference JARs:** keep extracted sources in `reference/extracted/` (gitignored) for porting; never drop them into player `mods/`.
- **Port map:** see `docs/integration/INTEGRATION_STATUS.md`.
