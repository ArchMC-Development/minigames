package gg.tropic.practice.extensions;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author Subham
 * @since 6/27/25
 */
public class PlayerMatchUtilities {
    public static List<String> getPlayerMatches(
            CommandSender sender,
            String input,
            Collection<Player> selectedPlayers
    ) {
        Player senderPlayer = sender instanceof Player ? (Player) sender : null;

        ArrayList<String> matchedPlayers = new ArrayList<>();
        for (Player player : selectedPlayers) {
            String name = player.getName();
            if ((senderPlayer == null || senderPlayer.canSee(player)) && StringUtil.startsWithIgnoreCase(name, input)) {
                matchedPlayers.add(name);
            }
        }


        matchedPlayers.sort(String.CASE_INSENSITIVE_ORDER);
        return matchedPlayers;
    }
}
