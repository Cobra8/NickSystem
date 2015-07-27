package me.Cobra_8.listeners;

import java.util.Iterator;
import me.Cobra_8.NickSystem;
import me.Cobra_8.objects.NickedPlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;

/**
 *
 * @author Cobra_8
 */
public class OnTabComplete implements Listener {

    @EventHandler
    public void onTabComplete(PlayerChatTabCompleteEvent e) {
        for (Iterator<String> iterator = e.getTabCompletions().iterator(); iterator.hasNext();) {
            String tabCompletion = iterator.next();
            if (NickSystem.getInstance().getNickedPlayers().containsKey(tabCompletion)) {
                iterator.remove();
            }
        }
        for (NickedPlayer nickedPlayer : NickSystem.getInstance().getNickedPlayers().values()) {
            if (nickedPlayer.getNick().startsWith(e.getLastToken())) {
                e.getTabCompletions().add(nickedPlayer.getNick());
            }
        }
    }

}
