package me.Cobra_8.commands;

import me.Cobra_8.NickSystem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 *
 * @author Cobra_8
 */
public class NickCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            NickSystem.getInstance().sendMessage("§cDu musst ein Spieler sein!", sender);
            return true;
        }
        if (!(sender.hasPermission("system.nick"))) {
            NickSystem.getInstance().sendMessage("§cDu hast dafür keine Rechte!", sender);
            return true;
        }
        Player player = (Player) sender;
        if (NickSystem.getInstance().isNicked(player.getName())) {
            NickSystem.getInstance().unnickPlayer(player.getName());
            NickSystem.getInstance().sendMessage("§7Du hast nun §ckeinen §7Nickname mehr!", sender);
        } else {
            NickSystem.getInstance().nickPlayer(player);
            NickSystem.getInstance().sendMessage("§7Du heisst nun: §c" + NickSystem.getInstance().getNickedPlayers().get(player.getName()).getNick(), sender);
        }
        return true;
    }

}
