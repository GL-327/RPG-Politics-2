package com.political.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * Capture/store/place real builds as {@link StructureTemplate} {@code .nbt} files. This is the
 * import pipeline for terrain-adaptive generation: an operator loads a world (e.g. the
 * "Old Fallen castle" save) and captures named buildings, which {@link SettlementGenerator}
 * can then splice into settlements instead of code-drawing them.
 *
 * <p>Templates live in {@code <gameDir>/rpg_structures/*.nbt} (writable at runtime) so captures
 * survive restarts and can be copied into a resource pack later.
 */
public final class StructureIO {

    private StructureIO() {}

    /** Writable structures folder in the server/game working directory. */
    public static Path dir() {
        Path d = Path.of("rpg_structures");
        try {
            Files.createDirectories(d);
        } catch (IOException e) {
            com.political.RpgPoliticsMod.LOGGER.warn("Could not create rpg_structures dir", e);
        }
        return d;
    }

    private static Path file(String name) {
        return dir().resolve(name.replaceAll("[^a-zA-Z0-9_\\-]", "_") + ".nbt");
    }

    /** True if a template of this name is stored on disk. */
    public static boolean exists(String name) {
        return Files.exists(file(name));
    }

    /** Lists the names (without extension) of every stored template. */
    public static List<String> list() {
        List<String> out = new ArrayList<>();
        Path d = dir();
        if (!Files.isDirectory(d)) return out;
        try (Stream<Path> s = Files.list(d)) {
            s.filter(p -> p.toString().endsWith(".nbt"))
                    .forEach(p -> out.add(p.getFileName().toString().replaceAll("\\.nbt$", "")));
        } catch (IOException ignored) {
        }
        out.sort(String::compareTo);
        return out;
    }

    /** Captures the inclusive region between two corners into a named template (entities included). */
    public static Vec3i capture(ServerLevel level, BlockPos a, BlockPos b, String name) throws IOException {
        BoundingBox box = BoundingBox.fromCorners(a, b);
        BlockPos origin = new BlockPos(box.minX(), box.minY(), box.minZ());
        Vec3i size = new Vec3i(box.getXSpan(), box.getYSpan(), box.getZSpan());

        StructureTemplate template = new StructureTemplate();
        // Empty ignore-list keeps interior air so captured buildings paste back faithfully.
        template.fillFromWorld(level, origin, size, true, List.of());
        CompoundTag tag = template.save(new CompoundTag());
        NbtIo.writeCompressed(tag, file(name));
        return size;
    }

    /** Loads a stored template, or returns {@code null} if it is missing/corrupt. */
    public static StructureTemplate load(ServerLevel level, String name) {
        Path f = file(name);
        if (!Files.exists(f)) return null;
        try {
            CompoundTag tag = NbtIo.readCompressed(f, NbtAccounter.unlimitedHeap());
            StructureTemplate template = new StructureTemplate();
            template.load(level.registryAccess().lookupOrThrow(Registries.BLOCK), tag);
            return template;
        } catch (IOException | RuntimeException e) {
            com.political.RpgPoliticsMod.LOGGER.error("Failed to load structure '{}'", name, e);
            return null;
        }
    }

    /** Places a stored template directly into the world; returns its footprint size or null. */
    public static Vec3i place(ServerLevel level, String name, BlockPos pos, Rotation rotation) {
        StructureTemplate template = load(level, name);
        if (template == null) return null;
        place(level, template, pos, rotation);
        return template.getSize(rotation);
    }

    /** Places an already-loaded template; pivot is its own centre so rotation stays put. */
    public static void place(ServerLevel level, StructureTemplate template, BlockPos pos, Rotation rotation) {
        Vec3i size = template.getSize();
        BlockPos pivot = new BlockPos(size.getX() / 2, 0, size.getZ() / 2);
        StructurePlaceSettings settings = new StructurePlaceSettings()
                .setRotation(rotation)
                .setRotationPivot(pivot)
                .setIgnoreEntities(false);
        template.placeInWorld(level, pos, pos, settings, level.getRandom(), 2);
    }
}
