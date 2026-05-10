package gg.solara.practice.migration;

import com.grinderwolf.swm.internal.com.flowpowered.nbt.ByteArrayTag;
import com.grinderwolf.swm.internal.com.flowpowered.nbt.ByteTag;
import com.grinderwolf.swm.internal.com.flowpowered.nbt.CompoundMap;
import com.grinderwolf.swm.internal.com.flowpowered.nbt.CompoundTag;
import com.grinderwolf.swm.internal.com.flowpowered.nbt.DoubleTag;
import com.grinderwolf.swm.internal.com.flowpowered.nbt.FloatTag;
import com.grinderwolf.swm.internal.com.flowpowered.nbt.IntArrayTag;
import com.grinderwolf.swm.internal.com.flowpowered.nbt.IntTag;
import com.grinderwolf.swm.internal.com.flowpowered.nbt.ListTag;
import com.grinderwolf.swm.internal.com.flowpowered.nbt.LongArrayTag;
import com.grinderwolf.swm.internal.com.flowpowered.nbt.LongTag;
import com.grinderwolf.swm.internal.com.flowpowered.nbt.ShortTag;
import com.grinderwolf.swm.internal.com.flowpowered.nbt.StringTag;
import com.grinderwolf.swm.internal.com.flowpowered.nbt.Tag;
import com.grinderwolf.swm.internal.com.flowpowered.nbt.TagType;
import com.grinderwolf.swm.internal.com.flowpowered.nbt.regionfile.Chunk;
import com.grinderwolf.swm.internal.com.flowpowered.nbt.regionfile.RegionFile;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Rewrites Paper 1.21 chunk NBT into the pre-1.18 layout SWM 2.2.1's {@code WorldImporter}
 * understands: wraps root fields under {@code Level{...}}, renames keys, lifts
 * {@code block_states.palette}/{@code data} into {@code Palette}/{@code BlockStates} per
 * section, strips modern Status namespaces, and forces {@code DataVersion = 2586} so the
 * downstream Mojang DataConverter pipeline runs through familiar territory.
 *
 * <p>Lossy: structures, mob entities, and unknown fields are dropped — sufficient for the
 * dev-tools use case (blocks + tile entities + heightmaps).
 */
public final class SwmChunkTranslator {

    private static final Logger LOGGER = Logger.getLogger(SwmChunkTranslator.class.getName());

    /** 1.16.5 — within SWM 2.2.1's known data-fixer range. */
    private static final int TARGET_DATA_VERSION = 2586;

    private SwmChunkTranslator() {
    }

    public static void translateRegionFolder(File regionDir) throws IOException {
        File[] files = regionDir.listFiles((dir, name) -> name.startsWith("r.") && name.endsWith(".mca"));
        if (files == null) {
            LOGGER.warning("No region files found in " + regionDir.getAbsolutePath());
            return;
        }
        LOGGER.info("Translating " + files.length + " region file(s) under " + regionDir.getAbsolutePath());
        int totalChunks = 0;
        for (File file : files) {
            totalChunks += translateRegionFile(file);
        }
        LOGGER.info("Rewrote " + totalChunks + " chunks total.");
    }

    private static int translateRegionFile(File file) throws IOException {
        HashMap<Integer, Chunk> translated = new HashMap<>();
        boolean loggedFirstStatus = false;
        try (RegionFile rf = RegionFile.open(file.toPath())) {
            for (Integer idx : rf.listChunks()) {
                Chunk chunk = rf.loadChunk(idx);
                if (chunk == null) continue;

                CompoundTag root;
                try {
                    root = chunk.readTag();
                } catch (Exception ex) {
                    LOGGER.log(Level.WARNING,
                        "Failed to read chunk index " + idx + " in " + file.getName() + " — skipping",
                        ex);
                    continue;
                }
                if (root == null) continue;

                if (!loggedFirstStatus) {
                    Tag<?> srcStatus = root.getValue().get("Status");
                    if (srcStatus == null) srcStatus = root.getValue().get("status");
                    LOGGER.info("First chunk in " + file.getName() + " — root keys=" + root.getValue().keySet()
                        + " ; srcStatus=" + (srcStatus == null ? "<absent>" : srcStatus.getValue()));
                }

                CompoundTag rewritten = rewriteChunk(root);

                if (!loggedFirstStatus) {
                    Tag<?> level = rewritten.getValue().get("Level");
                    if (level instanceof CompoundTag) {
                        Tag<?> outStatus = ((CompoundTag) level).getValue().get("Status");
                        LOGGER.info("After rewrite — Level keys=" + ((CompoundTag) level).getValue().keySet()
                            + " ; Status=" + (outStatus == null ? "<absent>" : outStatus.getValue()));
                    }
                    loggedFirstStatus = true;
                }

                translated.put(idx, new Chunk(chunk.x, chunk.z, chunk.timestamp, rewritten, chunk.getCompression()));
            }
        }

        // RegionFile.createNew expects a fresh path.
        if (!file.delete()) {
            throw new IOException("Could not delete original region file " + file.getName());
        }
        try (RegionFile fresh = RegionFile.createNew(file.toPath())) {
            fresh.writeChunks(translated);
        }
        return translated.size();
    }

    private static CompoundTag rewriteChunk(CompoundTag root) {
        CompoundMap rootValue = root.getValue();

        // Move everything except DataVersion into the inner Level map.
        CompoundMap levelMap = new CompoundMap();
        for (Tag<?> tag : rootValue.values()) {
            if (!"DataVersion".equals(tag.getName())) levelMap.put(tag);
        }

        renameInPlace(levelMap, "sections", "Sections");
        renameInPlace(levelMap, "block_entities", "TileEntities");
        renameInPlace(levelMap, "block_ticks", "TileTicks");
        renameInPlace(levelMap, "fluid_ticks", "LiquidTicks");
        renameInPlace(levelMap, "structures", "Structures");

        // SWM filters chunks whose Status doesn't startsWith("full"); modern Paper writes
        // "minecraft:full" — strip the namespace, or default to "full" when absent.
        Tag<?> status = levelMap.get("Status");
        if (status instanceof StringTag) {
            String value = ((StringTag) status).getValue();
            if (value != null && value.startsWith("minecraft:")) {
                levelMap.put(new StringTag("Status", value.substring("minecraft:".length())));
            }
        } else {
            levelMap.put(new StringTag("Status", "full"));
        }

        Tag<?> sectionsRaw = levelMap.get("Sections");
        if (sectionsRaw instanceof ListTag) {
            for (Object element : ((ListTag<?>) sectionsRaw).getValue()) {
                if (element instanceof CompoundTag) rewriteSection((CompoundTag) element);
            }
        }

        // SWM expects a flat int[1024] biome array on Level; default to plains when absent.
        if (!levelMap.containsKey("Biomes")) {
            int[] flatBiomes = new int[1024];
            Arrays.fill(flatBiomes, 1);
            levelMap.put(new IntArrayTag("Biomes", flatBiomes));
        }

        CompoundMap newRoot = new CompoundMap();
        newRoot.put(new IntTag("DataVersion", TARGET_DATA_VERSION));
        newRoot.put(new CompoundTag("Level", levelMap));
        return new CompoundTag("", newRoot);
    }

    private static void rewriteSection(CompoundTag section) {
        CompoundMap secValue = section.getValue();

        Tag<?> blockStatesRaw = secValue.remove("block_states");
        if (blockStatesRaw instanceof CompoundTag) {
            CompoundMap bs = ((CompoundTag) blockStatesRaw).getValue();

            Tag<?> palette = bs.get("palette");
            if (palette != null) secValue.put(renamed(palette, "Palette"));

            Tag<?> data = bs.get("data");
            if (data != null) {
                secValue.put(renamed(data, "BlockStates"));
            } else if (palette != null) {
                // Single-palette sections omit `data` on modern; SWM still expects the field.
                secValue.put(new LongArrayTag("BlockStates", new long[0]));
            }
        }

        secValue.remove("biomes");
    }

    private static void renameInPlace(CompoundMap map, String from, String to) {
        if (from.equals(to)) return;
        Tag<?> existing = map.remove(from);
        if (existing != null) map.put(renamed(existing, to));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Tag<?> renamed(Tag<?> source, String name) {
        if (name.equals(source.getName())) return source;
        TagType type = source.getType();
        switch (type) {
            case TAG_COMPOUND:
                return new CompoundTag(name, (CompoundMap) source.getValue());
            case TAG_LIST:
                ListTag listSrc = (ListTag) source;
                return new ListTag(name, listSrc.getElementType(), (List) listSrc.getValue());
            case TAG_INT:
                return new IntTag(name, ((Number) source.getValue()).intValue());
            case TAG_INT_ARRAY:
                return new IntArrayTag(name, (int[]) source.getValue());
            case TAG_LONG_ARRAY:
                return new LongArrayTag(name, (long[]) source.getValue());
            case TAG_BYTE_ARRAY:
                return new ByteArrayTag(name, (byte[]) source.getValue());
            case TAG_BYTE:
                return new ByteTag(name, ((Number) source.getValue()).byteValue());
            case TAG_SHORT:
                return new ShortTag(name, ((Number) source.getValue()).shortValue());
            case TAG_LONG:
                return new LongTag(name, ((Number) source.getValue()).longValue());
            case TAG_FLOAT:
                return new FloatTag(name, ((Number) source.getValue()).floatValue());
            case TAG_DOUBLE:
                return new DoubleTag(name, ((Number) source.getValue()).doubleValue());
            case TAG_STRING:
                return new StringTag(name, (String) source.getValue());
            default:
                return source;
        }
    }
}
