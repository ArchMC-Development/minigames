package gg.tropic.practice.schematics;

import com.cryptomorin.xseries.XMaterial;
import gg.tropic.practice.schematics.jnbt.*;
import gg.tropic.practice.schematics.manipulation.BlockChanger;
import gg.tropic.practice.schematics.manipulation.BlockOffset;
import me.lucko.helper.Schedulers;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;

public class SchematicUtil {

    private static final gg.tropic.practice.schematics.GridFSSchematicProvider SCHEMATIC_PROVIDER =
            new GridFSSchematicProvider();

    public static GridFSSchematicProvider getSchematicProvider() {
        return SCHEMATIC_PROVIDER;
    }

    public static void pasteSchematic(Location loc, Schematic schematic) {
        short[] blocks = schematic.getBlocks();
        byte[] blockData = schematic.getData();

        short length = schematic.getLength();
        short width = schematic.getWidth();
        short height = schematic.getHeight();

        BlockOffset offset = schematic.getOffset();

        // Apply offset
        Location pasteLocation = loc.clone();
        pasteLocation.setX(pasteLocation.getX() + offset.getX());
        pasteLocation.setY(pasteLocation.getY() + offset.getY());
        pasteLocation.setZ(pasteLocation.getZ() + offset.getZ());

        // Create set of block snapshots for BlockChanger
        Set<BlockChanger.BlockSnapshot> blockSnapshots = new HashSet<>();

        // Convert schematic data to BlockChanger format
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    int index = y * width * length + z * width + x;

                    if (index >= blocks.length || index >= blockData.length) continue;

                    short blockId = blocks[index];
                    byte data = blockData[index];

                    // Skip air blocks (optional)
                    if (blockId == 0) continue;

                    Location blockLocation = new Location(
                            pasteLocation.getWorld(),
                            x + pasteLocation.getX(),
                            y + pasteLocation.getY(),
                            z + pasteLocation.getZ()
                    );

                    Material material = getNullableMaterial(blockId, data);
                    if (material != null) {
                        blockSnapshots.add(new BlockChanger.BlockSnapshot(blockLocation, material, data));
                    }
                }
            }
        }

        // Paste blocks using BlockChanger
        BlockChanger.setBlocks(pasteLocation.getWorld(), blockSnapshots);
        handleTileEntities(pasteLocation, schematic);
    }

    @SuppressWarnings("deprecation")
    private static Material getNullableMaterial(short blockId, byte data) {
        try {
            return Objects.requireNonNullElse(Material.getMaterial(blockId), XMaterial.STONE.parseMaterial());
        } catch (Exception e) {
            Bukkit.getLogger().warning("Error getting material for block ID " + blockId + ": " + e.getMessage());
            return XMaterial.STONE.parseMaterial(); // Fallback
        }
    }

    public static void clearSchematic(Location loc, Schematic schematic) {
        short length = schematic.getLength();
        short width = schematic.getWidth();
        short height = schematic.getHeight();

        BlockOffset offset = schematic.getOffset();

        // Apply offset
        Location pasteLocation = loc.clone();
        pasteLocation.setX(pasteLocation.getX() + offset.getX());
        pasteLocation.setY(pasteLocation.getY() + offset.getY());
        pasteLocation.setZ(pasteLocation.getZ() + offset.getZ());

        // Create set of block snapshots for BlockChanger
        Set<BlockChanger.BlockSnapshot> blockSnapshots = new HashSet<>();

        // Convert schematic data to BlockChanger format
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                for (int z = 0; z < length; z++) {
                    Location blockLocation = new Location(
                            pasteLocation.getWorld(),
                            x + pasteLocation.getX(),
                            y + pasteLocation.getY(),
                            z + pasteLocation.getZ()
                    );
                    blockSnapshots.add(new BlockChanger.BlockSnapshot(blockLocation, Material.AIR));
                }
            }
        }

        // Paste blocks using BlockChanger
        BlockChanger.setBlocks(pasteLocation.getWorld(), blockSnapshots);
        handleTileEntities(pasteLocation, schematic);
    }

    /**
     * Paste a schematic from GridFS storage by name
     *
     * @param location      The location to paste at
     * @param schematicName The name of the schematic in storage
     * @return CompletableFuture<Boolean> indicating success
     */
    public static CompletableFuture<Boolean> pasteSchematicFromStorage(Location location, String schematicName) {
        return SCHEMATIC_PROVIDER.loadSchematic(schematicName)
                .thenComposeAsync(schematic -> {
                    if (schematic == null) {
                        Bukkit.getLogger().warning("Schematic not found: " + schematicName);
                        return CompletableFuture.completedFuture(false);
                    }

                    // Schedule pasting on main thread
                    return CompletableFuture.supplyAsync(() -> {
                        pasteSchematic(location, schematic);
                        return true;
                    });
                });
    }

    // ========== LOADING METHODS ==========

    public static Schematic loadSchematicFromInputStream(InputStream inputStream) {
        try {
            NBTInputStream nbtStream = new NBTInputStream(inputStream, true);

            CompoundTag schematicTag = (CompoundTag) nbtStream.readTag();
            if (!schematicTag.getName().equals("Schematic")) {
                throw new IllegalArgumentException("Tag \"Schematic\" does not exist or is not first");
            }

            Map<String, Tag> schematic = schematicTag.getValue();
            if (!schematic.containsKey("Blocks")) {
                throw new IllegalArgumentException("Schematic file is missing a \"Blocks\" tag");
            }

            short width = getChildTag(schematic, "Width", ShortTag.class).getValue();
            short length = getChildTag(schematic, "Length", ShortTag.class).getValue();
            short height = getChildTag(schematic, "Height", ShortTag.class).getValue();

            byte[] blockId = getChildTag(schematic, "Blocks", ByteArrayTag.class).getValue();
            byte[] blockData = getChildTag(schematic, "Data", ByteArrayTag.class).getValue();
            byte[] addId = new byte[0];
            short[] blocks = new short[blockId.length];

            BlockOffset offset = new BlockOffset(0, 0, 0);
            if (schematic.containsKey("WEOffsetX") && schematic.containsKey("WEOffsetY") && schematic.containsKey("WEOffsetZ")) {
                int offsetX = getChildTag(schematic, "WEOffsetX", IntTag.class).getValue();
                int offsetY = getChildTag(schematic, "WEOffsetY", IntTag.class).getValue();
                int offsetZ = getChildTag(schematic, "WEOffsetZ", IntTag.class).getValue();
                offset = new BlockOffset(offsetX, offsetY, offsetZ);
            }

            List<Tag> tileentities = getChildTag(schematic, "TileEntities", ListTag.class).getValue();

            if (schematic.containsKey("AddBlocks")) {
                addId = getChildTag(schematic, "AddBlocks", ByteArrayTag.class).getValue();
            }

            for (int index = 0; index < blockId.length; index++) {
                if ((index >> 1) >= addId.length) {
                    blocks[index] = (short) (blockId[index] & 0xFF);
                } else {
                    if ((index & 1) == 0) {
                        blocks[index] = (short) (((addId[index >> 1] & 0x0F) << 8) + (blockId[index] & 0xFF));
                    } else {
                        blocks[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (blockId[index] & 0xFF));
                    }
                }
            }

            return new Schematic(blocks, blockData, tileentities, width, length, height, offset);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }


    public static Schematic loadSchematic(File file) {
        try {
            FileInputStream stream = new FileInputStream(file);
            NBTInputStream nbtStream = new NBTInputStream(stream, true);

            CompoundTag schematicTag = (CompoundTag) nbtStream.readTag();
            if (!schematicTag.getName().equals("Schematic")) {
                throw new IllegalArgumentException("Tag \"Schematic\" does not exist or is not first");
            }

            Map<String, Tag> schematic = schematicTag.getValue();
            if (!schematic.containsKey("Blocks")) {
                throw new IllegalArgumentException("Schematic file is missing a \"Blocks\" tag");
            }

            short width = getChildTag(schematic, "Width", ShortTag.class).getValue();
            short length = getChildTag(schematic, "Length", ShortTag.class).getValue();
            short height = getChildTag(schematic, "Height", ShortTag.class).getValue();

            byte[] blockId = getChildTag(schematic, "Blocks", ByteArrayTag.class).getValue();
            byte[] blockData = getChildTag(schematic, "Data", ByteArrayTag.class).getValue();
            byte[] addId = new byte[0];
            short[] blocks = new short[blockId.length];

            BlockOffset offset = new BlockOffset(0, 0, 0);
            if (schematic.containsKey("WEOffsetX") && schematic.containsKey("WEOffsetY") && schematic.containsKey("WEOffsetZ")) {
                int offsetX = getChildTag(schematic, "WEOffsetX", IntTag.class).getValue();
                int offsetY = getChildTag(schematic, "WEOffsetY", IntTag.class).getValue();
                int offsetZ = getChildTag(schematic, "WEOffsetZ", IntTag.class).getValue();
                offset = new BlockOffset(offsetX, offsetY, offsetZ);
            }

            List<Tag> tileentities = getChildTag(schematic, "TileEntities", ListTag.class).getValue();

            if (schematic.containsKey("AddBlocks")) {
                addId = getChildTag(schematic, "AddBlocks", ByteArrayTag.class).getValue();
            }

            for (int index = 0; index < blockId.length; index++) {
                if ((index >> 1) >= addId.length) {
                    blocks[index] = (short) (blockId[index] & 0xFF);
                } else {
                    if ((index & 1) == 0) {
                        blocks[index] = (short) (((addId[index >> 1] & 0x0F) << 8) + (blockId[index] & 0xFF));
                    } else {
                        blocks[index] = (short) (((addId[index >> 1] & 0xF0) << 4) + (blockId[index] & 0xFF));
                    }
                }
            }

            return new Schematic(blocks, blockData, tileentities, width, length, height, offset);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Load a schematic from GridFS storage
     *
     * @param schematicName The name of the schematic
     * @return CompletableFuture<Schematic>
     */
    public static CompletableFuture<Schematic> loadSchematicFromStorage(String schematicName) {
        return SCHEMATIC_PROVIDER.loadSchematic(schematicName);
    }

    // ========== SAVING METHODS ==========

    /**
     * Save a schematic file to GridFS storage
     *
     * @param schematicName The name to store it as
     * @param schematicFile The schematic file
     * @return CompletableFuture<Boolean> indicating success
     */
    public static CompletableFuture<?> saveSchematicToStorage(String schematicName, File schematicFile) {
        return SCHEMATIC_PROVIDER.saveSchematic(schematicName, schematicFile);
    }

    /**
     * Save schematic data to GridFS storage
     *
     * @param schematicName The name to store it as
     * @param schematicData The raw schematic bytes
     * @return CompletableFuture<Boolean> indicating success
     */
    public static CompletableFuture<?> saveSchematicToStorage(String schematicName, byte[] schematicData) {
        return SCHEMATIC_PROVIDER.saveSchematic(schematicName, schematicData);
    }

    /**
     * Save player's current WorldEdit clipboard as a schematic
     *
     * @param player        The player whose clipboard to save
     * @param schematicName The name for the schematic
     * @return CompletableFuture<Boolean> indicating success
     */
    public static CompletableFuture<?> saveClipboardToStorage(Player player, String schematicName) {
        return SCHEMATIC_PROVIDER.saveFromClipboard(player, schematicName);
    }

    // ========== MANAGEMENT METHODS ==========

    /**
     * Check if a schematic exists in storage
     *
     * @param schematicName The name of the schematic
     * @return CompletableFuture<Boolean>
     */
    public static CompletableFuture<Boolean> schematicExistsInStorage(String schematicName) {
        return SCHEMATIC_PROVIDER.schematicExists(schematicName);
    }

    /**
     * Delete a schematic from storage
     *
     * @param schematicName The name of the schematic
     * @return CompletableFuture<Boolean> indicating success
     */
    public static CompletableFuture<Boolean> deleteSchematicFromStorage(String schematicName) {
        return SCHEMATIC_PROVIDER.deleteSchematic(schematicName);
    }

    /**
     * List all available schematics in storage
     *
     * @return CompletableFuture<List < String>>
     */
    public static CompletableFuture<List<String>> listStoredSchematics() {
        return SCHEMATIC_PROVIDER.listSchematics();
    }

    // ========== HELPER METHODS ==========

    private static void handleTileEntities(Location loc, Schematic schematic) {
        Schedulers
                .sync()
                .runLater(() -> {
                    for (Tag t : schematic.getTileentities()) {
                        CompoundTag ct = (CompoundTag) t;
                        Map<String, Tag> tiledata = ct.getValue();

                        int x = getChildTag(tiledata, "x", IntTag.class).getValue() + (int) loc.getX();
                        int y = getChildTag(tiledata, "y", IntTag.class).getValue() + (int) loc.getY();
                        int z = getChildTag(tiledata, "z", IntTag.class).getValue() + (int) loc.getZ();

                        Location tileLocation = new Location(loc.getWorld(), x, y, z);

                        // Apply tile entity data using Bukkit API for better compatibility
                        applyTileEntityData(tileLocation, tiledata);
                    }
                }, 5L)
                .join();
    }

    private static void applyTileEntityData(Location location, Map<String, Tag> tiledata) {
        // This method can be expanded based on your needs
        // For now, it's a placeholder that handles basic tile entity types
        try {
            String id = getChildTag(tiledata, "id", StringTag.class).getValue();

            switch (id.toLowerCase()) {
                case "sign":
                case "minecraft:sign":
                    // Handle signs
                    handleSign(location, tiledata);
                    break;
                case "chest":
                case "minecraft:chest":
                    // Handle chests
                    handleChest(location, tiledata);
                    break;
                // Add more tile entity types as needed
                default:
                    Bukkit.getLogger().info("Unhandled tile entity type: " + id + " at " + location);
                    break;
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("Error processing tile entity at " + location + ": " + e.getMessage());
        }
    }

    private static void handleSign(Location location, Map<String, Tag> tiledata) {
        // Basic sign handling - can be expanded
        try {
            if (location.getBlock().getState() instanceof org.bukkit.block.Sign) {
                org.bukkit.block.Sign sign = (org.bukkit.block.Sign) location.getBlock().getState();

                // Try to get text lines
                for (int i = 1; i <= 4; i++) {
                    String lineKey = "Text" + i;
                    if (tiledata.containsKey(lineKey)) {
                        String text = getChildTag(tiledata, lineKey, StringTag.class).getValue();
                        if (text.equals("\"\"")) {
                            continue;
                        }

                        // Clean up JSON formatting if present
                        text = text.replace("{\"extra\":[\"", "");
                        text = text.replace("\"],\"text\":\"\"}", "");
                        sign.setLine(i - 1, text);
                    }
                }
                sign.update();
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("Error handling sign: " + e.getMessage());
        }
    }

    private static void handleChest(Location location, Map<String, Tag> tiledata) {
        // Basic chest handling - can be expanded
        try {
            if (location.getBlock().getState() instanceof org.bukkit.block.Chest) {
                // Chest inventory handling can be implemented here
                // This would require parsing the Items list from the NBT data
            }
        } catch (Exception e) {
            Bukkit.getLogger().warning("Error handling chest: " + e.getMessage());
        }
    }


    private static <T extends Tag> T getChildTag(Map<String, Tag> items, String key, Class<T> expected)
            throws IllegalArgumentException {
        if (!items.containsKey(key)) {
            throw new IllegalArgumentException("Schematic file is missing a \"" + key + "\" tag");
        }
        Tag tag = items.get(key);
        if (!expected.isInstance(tag)) {
            throw new IllegalArgumentException(key + " tag is not of tag type " + expected.getName());
        }

        return expected.cast(tag);
    }
}
