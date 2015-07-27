package me.Cobra_8.listeners;

import me.Cobra_8.NickSystem;
import me.Cobra_8.objects.NickedPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

/**
 *
 * @author Cobra_8
 */
public class OnJoin implements Listener {

    @EventHandler
    public void onJoin(final PlayerJoinEvent e) {
        if (NickSystem.getInstance().getNickedPlayers().containsKey(e.getPlayer().getName())) {
            NickSystem.getInstance().getNickedPlayers().get(e.getPlayer().getName()).nick();
        }
        for (NickedPlayer nickedPlayer : NickSystem.getInstance().getNickedPlayers().values()) {
            nickedPlayer.sendPackets(e.getPlayer());
        }
    }
}
