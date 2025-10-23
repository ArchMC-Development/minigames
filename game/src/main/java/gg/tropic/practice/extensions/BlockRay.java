package gg.tropic.practice.extensions;

import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class BlockRay implements Iterator<Block> {
    private final World world;
    private final double deltaX, deltaY, deltaZ;
    private final double stepX, stepY, stepZ;
    private final int maxSteps;

    private double currentX, currentY, currentZ;
    private int step;
    private int lastBlockX = Integer.MIN_VALUE, lastBlockY = Integer.MIN_VALUE, lastBlockZ = Integer.MIN_VALUE;

    public BlockRay(World world, Vector src, Vector dst, double stepSize) {
        this.world = world;

        // Calculate delta once
        this.deltaX = dst.getX() - src.getX();
        this.deltaY = dst.getY() - src.getY();
        this.deltaZ = dst.getZ() - src.getZ();

        double length = Math.sqrt(deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ);
        if (length == 0) {
            throw new IllegalArgumentException("Source and destination are the same");
        }

        this.maxSteps = (int) Math.ceil(length / stepSize);
        double normalizedStep = 1.0 / maxSteps;

        // Pre-calculate step increments
        this.stepX = deltaX * normalizedStep;
        this.stepY = deltaY * normalizedStep;
        this.stepZ = deltaZ * normalizedStep;

        // Start at source
        this.currentX = src.getX();
        this.currentY = src.getY();
        this.currentZ = src.getZ();
        this.step = 0;
    }

    @Override
    public boolean hasNext() {
        return step <= maxSteps;
    }

    @Override
    public Block next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No more blocks");
        }

        // Skip duplicate blocks
        int blockX, blockY, blockZ;
        do {
            blockX = NumberConversions.floor(currentX);
            blockY = NumberConversions.floor(currentY);
            blockZ = NumberConversions.floor(currentZ);

            currentX += stepX;
            currentY += stepY;
            currentZ += stepZ;
            step++;
        } while (step <= maxSteps && blockX == lastBlockX && blockY == lastBlockY && blockZ == lastBlockZ);

        lastBlockX = blockX;
        lastBlockY = blockY;
        lastBlockZ = blockZ;

        return world.getBlockAt(blockX, blockY, blockZ);
    }
}
