package gg.solara.practice.utilities;

import com.sk89q.worldedit.CuboidClipboard;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.EditSessionFactory;
import com.sk89q.worldedit.Vector;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.schematic.SchematicFormat;


import org.bukkit.World;

import java.io.File;

public final class WorldEditUtils {

    public static EditSession primeWorldEditApi(World world) {
        EditSessionFactory esFactory = WorldEdit.getInstance().getEditSessionFactory();

        var worldEditWorld = new BukkitWorld(world);
        return esFactory.getEditSession(worldEditWorld, Integer.MAX_VALUE);
    }

    public static CuboidClipboard paste(World world, File schematic, Vector pasteAt) throws Exception {
        var editSession = primeWorldEditApi(world);
        CuboidClipboard clipboard = SchematicFormat.MCEDIT.load(schematic);

        // systems like the ArenaGrid assume that pastes will 'begin' directly at the Vector
        // provided. to ensure we can do this, we manually clear any offset (distance from
        // corner of schematic to player) to ensure our pastes aren't dependant on the
        // location of the player when copied
        clipboard.setOffset(new Vector(0, 0, 0));
        clipboard.paste(editSession, pasteAt, true);

        return clipboard;
    }

}
