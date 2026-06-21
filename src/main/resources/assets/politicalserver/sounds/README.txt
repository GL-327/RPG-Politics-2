RPG Politics 2 — custom audio drop folder
==========================================

This folder is where real .ogg sound files for the mod's custom sound events
should be placed once they are authored. It is intentionally a placeholder for
now: the mod ships with ZERO custom .ogg files and instead maps every custom
sound event to layered VANILLA sounds via ../sounds.json (each entry uses
"type": "event"). This guarantees nothing is ever silent or broken.

HOW TO UPGRADE A SOUND TO REAL AUDIO
------------------------------------
1. Encode your audio as mono Ogg Vorbis (.ogg). Recommended layout, mirroring
   the custom sound ids registered in com.political.sound.ModSounds:

       sounds/power/cast_fire.ogg
       sounds/power/cast_frost.ogg
       sounds/ui/click.ogg
       sounds/mob/spirit_screech.ogg
       sounds/dungeon/trap.ogg
       ... etc.

2. In ../sounds.json, change the matching entry's "sounds" array from a vanilla
   event reference:

       { "name": "minecraft:ui.button.click", "type": "event" }

   to a file reference (default type, can be omitted):

       { "name": "politicalserver:ui/click" }

   The "name" is the .ogg path under this folder WITHOUT the ".ogg" extension
   and WITHOUT the leading "sounds/".

3. Leave the Java side untouched — ModSounds already registers the sound event
   ids; only the client-side sounds.json mapping decides what audio plays.

See docs/expansion3/sounds.md for the full id list and integration call sites.
