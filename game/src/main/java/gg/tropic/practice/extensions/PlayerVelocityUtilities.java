package gg.tropic.practice.extensions;

import org.bukkit.Location;
import org.bukkit.entity.Player;

/**
 * @author GrowlyX
 * @since 3/21/2025
 */
public class PlayerVelocityUtilities {
    public static void pushAway(Player player, Location location, double vertical, double horizontal) {
        final Location loc = player.getLocation();

        double hf1 = Math.max(-4, Math.min(4, vertical));
        double rf1 = Math.max(-4, Math.min(4, -1 * horizontal));

        player.setVelocity(location.toVector().subtract(loc.toVector()).normalize().multiply(rf1).setY(hf1));
    }
}
