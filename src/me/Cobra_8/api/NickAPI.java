package me.Cobra_8.api;

import me.Cobra_8.NickSystem;
import org.bukkit.entity.Player;

/**
 *
 * @author Cobra_8
 */
public class NickAPI {

    public static final void nickPlayer(String name) {
        NickSystem.getInstance().nickPlayer(name);
    }

    public static final void nickPlayer(Player player) {
        NickSystem.getInstance().nickPlayer(player);
    }

    public static final void unnickPlayer(String name) {
        NickSystem.getInstance().unnickPlayer(name);
    }

    public static final void unnickPlayer(Player player) {
        unnickPlayer(player.getName());
    }

    public static final boolean isNicked(String name) {
        return NickSystem.getInstance().isNicked(name);
    }

    public static final boolean isNicked(Player player) {
        return isNicked(player.getName());
    }

    public static final String getNickname(String name) {
        if (!(isNicked(name))) {
            return name;
        }
        return NickSystem.getInstance().getNickedPlayers().get(name).getNick();
    }

    public static final String getNickname(Player player) {
        return getNickname(player.getName());
    }

}
