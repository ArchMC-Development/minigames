package gg.tropic.practice.extensions;

import com.cryptomorin.xseries.XMaterial;
import net.evilblock.cubed.util.ServerVersion;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

/**
 * Using direct line-of-sight calculation
 */
public class BlastProtectionUtil {

    public static boolean isBedBlock(Material material) {
        if (ServerVersion.getVersion().isNewerThan(ServerVersion.v1_19)) {
            return material.name().endsWith("_BED");
        }

        return material == Material.BED_BLOCK;
    }

    public static boolean isGlass(Material material) {
        if (ServerVersion.getVersion().isNewerThan(ServerVersion.v1_19)) {
            return material.name().endsWith("GLASS");
        }

        return material == Material.GLASS;
    }

    /**
     * Ultra-fast protection check using Bresenham-like 3D line algorithm
     * This eliminates the iterator overhead entirely
     */
    public static boolean isProtected(Location pov, @NotNull Block block, double step) {
        if (!block.hasMetadata("placed") || isBedBlock(block.getType())) {
            return true;
        }

        final World world = block.getWorld();
        final Vector start = pov.toVector();
        final Vector end = block.getLocation().toVector();

        // Simple center-to-center ray check first
        if (isDirectLineClear(world, start, end, step)) {
            return false;
        }

        // Check a few offset rays if center is blocked
        Vector[] offsets = {
                new Vector(0.3, 0, 0),
                new Vector(-0.3, 0, 0),
                new Vector(0, 0.3, 0),
                new Vector(0, -0.3, 0)
        };

        int clearCount = 0;
        for (Vector offset : offsets) {
            if (isDirectLineClear(world, start.clone().add(offset), end, step)) {
                clearCount++;
                if (clearCount >= 2) {
                    return false;
                }
            }
        }

        return true;
    }

    /**
     * Direct line-of-sight check without iterator overhead
     */
    private static boolean isDirectLineClear(World world, Vector start, Vector end, double step) {
        double dx = end.getX() - start.getX();
        double dy = end.getY() - start.getY();
        double dz = end.getZ() - start.getZ();

        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (length == 0) return true;

        int steps = (int) Math.ceil(length / step);
        double stepX = dx / steps;
        double stepY = dy / steps;
        double stepZ = dz / steps;

        double currentX = start.getX();
        double currentY = start.getY();
        double currentZ = start.getZ();

        int lastBlockX = Integer.MIN_VALUE;
        int lastBlockY = Integer.MIN_VALUE;
        int lastBlockZ = Integer.MIN_VALUE;

        for (int i = 0; i <= steps; i++) {
            int blockX = NumberConversions.floor(currentX);
            int blockY = NumberConversions.floor(currentY);
            int blockZ = NumberConversions.floor(currentZ);

            // Skip duplicate blocks
            if (blockX != lastBlockX || blockY != lastBlockY || blockZ != lastBlockZ) {
                Block block = world.getBlockAt(blockX, blockY, blockZ);
                Material type = block.getType();

                if (type != Material.AIR) {
                    if (isGlass(type) || isBedBlock(type) || !block.hasMetadata("placed")) {
                        return false;
                    }
                }

                lastBlockX = blockX;
                lastBlockY = blockY;
                lastBlockZ = blockZ;
            }

            currentX += stepX;
            currentY += stepY;
            currentZ += stepZ;
        }

        return true;
    }
}
