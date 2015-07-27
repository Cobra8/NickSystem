package me.Cobra_8;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.Cobra_8.commands.NickCommand;
import me.Cobra_8.listeners.OnJoin;
import me.Cobra_8.listeners.OnTabComplete;
import me.Cobra_8.objects.NickedPlayer;
import me.Cobra_8.objects.gameProfile.ProfileBuilder;
import me.Cobra_8.objects.mysql.MySQL;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author Cobra_8
 */
public class NickSystem extends JavaPlugin {

    private static NickSystem instance;

    private ProfileBuilder profileBuilder;

    private Map<String, NickedPlayer> nickedPlayers;

    private File configFile;
    private FileConfiguration configuration;

    private MySQL mySQL;

    @Override
    public void onEnable() {
        instance = this;
        loadConfiguration();
        loadMySQL();
        registerCommands();
        registerListeners();
        profileBuilder = new ProfileBuilder();
        nickedPlayers = new HashMap<>();
    }

    public static NickSystem getInstance() {
        return instance;
    }

    public ProfileBuilder getProfileBuilder() {
        return profileBuilder;
    }

    public Map<String, NickedPlayer> getNickedPlayers() {
        return nickedPlayers;
    }

    public FileConfiguration getConfiguration() {
        return configuration;
    }

    public MySQL getMySQL() {
        return mySQL;
    }

    public void nickPlayer(String name) {
        NickedPlayer nickedPlayer;
        if (Bukkit.getPlayer(name) != null) {
            Player target = Bukkit.getPlayer(name);
            nickedPlayer = new NickedPlayer(target.getName(), target.getUniqueId());
            nickedPlayer.nick();
        } else {
            try {
                nickedPlayer = new NickedPlayer(name);
            } catch (IOException ex) {
                return;
            }
        }
        nickedPlayers.put(name, nickedPlayer);
    }

    public void nickPlayer(Player player) {
        NickedPlayer nickedPlayer = new NickedPlayer(player.getName(), player.getUniqueId());
        nickedPlayer.nick();
        nickedPlayers.put(player.getName(), nickedPlayer);
    }

    public void nickPlayer(Player player, String nick) {
        NickedPlayer nickedPlayer = new NickedPlayer(player.getName(), player.getUniqueId(), nick);
        nickedPlayer.nick();
        nickedPlayers.put(player.getName(), nickedPlayer);
    }

    public boolean isNicked(String name) {
        return nickedPlayers.containsKey(name);
    }

    public void unnickPlayer(String name) {
        if (!(nickedPlayers.containsKey(name))) {
            return;
        }
        NickedPlayer nickedPlayer = nickedPlayers.get(name);
        nickedPlayer.unnick();
        nickedPlayers.remove(name);
    }

    private void loadConfiguration() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdir();
        }
        this.configFile = new File(getInstance().getDataFolder(), "config.yml");
        this.configuration = YamlConfiguration.loadConfiguration(this.configFile);

        this.configuration.addDefault("MySQL.host", "host");
        this.configuration.addDefault("MySQL.port", 3306);
        this.configuration.addDefault("MySQL.user", "user");
        this.configuration.addDefault("MySQL.password", "pass");
        this.configuration.addDefault("MySQL.database", "database");

        this.configuration.addDefault("Update.delay", 2500L);

        this.configuration.options().copyDefaults(true);
        try {
            this.configuration.save(configFile);
        } catch (IOException ex) {
            Logger.getLogger(NickSystem.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void loadMySQL() {
        try {
            mySQL = new MySQL();
        } catch (SQLException ex) {
            mySQL = null;
            System.out.println("Error while connecting to MYSQL-Database!");
            System.out.println(ex.getMessage());
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    public String getPrefix() {
        return "§7[§cNickSystem§7] ";
    }

    public void sendMessage(String message, CommandSender sender) {
        sender.sendMessage(getPrefix() + message);
    }

    public void broadcastMessage(String message) {
        Bukkit.broadcastMessage(getPrefix() + message);
    }

    private void registerCommands() {
        getCommand("nick").setExecutor(new NickCommand());
    }

    private void registerListeners() {
        PluginManager pm = Bukkit.getPluginManager();
        pm.registerEvents(new OnJoin(), this);
        pm.registerEvents(new OnTabComplete(), this);
    }
}
