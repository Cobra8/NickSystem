package me.Cobra_8.objects;

import com.mojang.authlib.GameProfile;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import me.Cobra_8.NickSystem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

/**
 *
 * @author Cobra_8
 */
public class NickedPlayer {

    private final String name;
    private final UUID uniqueID;
    private String nick;

    private Object packetPlayOutEntityDestroy;
    private Object removeProfilePacket;

    private Object addProfilePacket;
    private Object packetPlayOutNamedEntitySpawn;

    public NickedPlayer(String name) throws IOException {
        this.name = name;
        this.uniqueID = NickSystem.getInstance().getProfileBuilder().getFetcher().getUniqueID(name);
        this.nick = ChatColor.stripColor(getRandomPlayerName());
    }

    public NickedPlayer(String name, UUID uniqueID) {
        this.name = name;
        this.uniqueID = uniqueID;
        this.nick = ChatColor.stripColor(getRandomPlayerName());
    }

    public NickedPlayer(String name, String nick) throws IOException {
        this.name = name;
        this.uniqueID = NickSystem.getInstance().getProfileBuilder().getFetcher().getUniqueID(name);
        this.nick = ChatColor.stripColor(nick);
    }

    public NickedPlayer(String name, UUID uniqueID, String nick) {
        this.name = name;
        this.uniqueID = uniqueID;
        this.nick = ChatColor.stripColor(nick);
    }

    private String getRandomPlayerName() {
        try {
            ResultSet set = NickSystem.getInstance().getMySQL().executeQuery("SELECT name FROM ns_nicknames ORDER BY RAND() LIMIT 0,1");
            if (set == null || !(set.first())) {
                return "Cobra_8";
            }
            return set.getString("name");
        } catch (SQLException ex) {
            return "Cobra_8";
        }
    }

    public void nick() {
        getPlayer().setDisplayName(getNick());
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    createPackets();
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        sendRemovePacketsSync(player);
                    }
                    Thread.sleep(50);
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        sendAddPacketsSync(player);
                    }
                } catch (IllegalAccessException | IOException | NoSuchMethodException | IllegalArgumentException | InvocationTargetException | NoSuchFieldException | ClassNotFoundException | InstantiationException | InterruptedException ex) {
                    Logger.getLogger(NickedPlayer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    public void unnick() {
        setNick(getName());
        nick();
    }

    private void createPackets() throws IllegalAccessException, IOException, NoSuchMethodException, IllegalArgumentException, InvocationTargetException, NoSuchFieldException, ClassNotFoundException, InstantiationException {
        Object entityPlayer = getBukkitClass("entity.CraftPlayer").getMethod("getHandle").invoke(getPlayer());
        UUID targetID = NickSystem.getInstance().getProfileBuilder().getFetcher().getUniqueID(getNick());
        GameProfile profile;
        if (targetID == null) {
            profile = new GameProfile(getUniqueID(), this.getNick());
        } else {
            profile = NickSystem.getInstance().getProfileBuilder().build(targetID);
            getField(GameProfile.class, "id").set(profile, getUniqueID());
        }

        int[] entID = new int[]{(int) entityPlayer.getClass().getMethod("getId").invoke(entityPlayer)};
        packetPlayOutEntityDestroy = getNmsClass("PacketPlayOutEntityDestroy").getConstructor(int[].class).newInstance((Object) entID);

        Constructor<?> playerInfoPacketConstructor = getNmsClass("PacketPlayOutPlayerInfo").getConstructor();
        Constructor<?> playerInfoDataConstructor = getNmsClass("PacketPlayOutPlayerInfo$PlayerInfoData").getConstructor(getNmsClass("PacketPlayOutPlayerInfo"), GameProfile.class, int.class, getNmsClass("WorldSettings$EnumGamemode"), getNmsClass("IChatBaseComponent"));

        removeProfilePacket = playerInfoPacketConstructor.newInstance();
        //erstes Argument die Umschliessende Klasse, da die Klasse PlayerInfoData eine eingenistete/static Klasse in PacketPlayOutPlayerInfo ist (nested class, static)
        Object removeInfoData = playerInfoDataConstructor.newInstance(removeProfilePacket, entityPlayer.getClass().getMethod("getProfile").invoke(entityPlayer), -1, null, null);
        getField(removeProfilePacket.getClass(), "a").set(removeProfilePacket, getNmsClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction").getEnumConstants()[4]);
        getField(removeProfilePacket.getClass(), "b").set(removeProfilePacket, Arrays.asList(removeInfoData));

        addProfilePacket = playerInfoPacketConstructor.newInstance();
        Object playerInteractManager = entityPlayer.getClass().getField("playerInteractManager").get(entityPlayer);
        Object gameMode = playerInteractManager.getClass().getMethod("getGameMode").invoke(playerInteractManager);
        Object[] craftChatMessage = (Object[]) getBukkitClass("util.CraftChatMessage").getMethod("fromString", String.class).invoke(null, this.getNick());
        //erstes Argument die Umschliessende Klasse, da die Klasse PlayerInfoData eine eingenistete/static Klasse in PacketPlayOutPlayerInfo ist (nested class, static)
        Object addInfoData = playerInfoDataConstructor.newInstance(addProfilePacket, profile, entityPlayer.getClass().getField("ping").get(entityPlayer), gameMode, craftChatMessage[0]);
        getField(addProfilePacket.getClass(), "a").set(addProfilePacket, getNmsClass("PacketPlayOutPlayerInfo$EnumPlayerInfoAction").getEnumConstants()[0]);
        getField(addProfilePacket.getClass(), "b").set(addProfilePacket, Arrays.asList(addInfoData));

        packetPlayOutNamedEntitySpawn = getNmsClass("PacketPlayOutNamedEntitySpawn").getConstructor(getNmsClass("EntityHuman")).newInstance(entityPlayer);
        getField(packetPlayOutNamedEntitySpawn.getClass(), "b").set(packetPlayOutNamedEntitySpawn, profile.getId());
    }

    public final void sendPackets(final Player player) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    sendRemovePacketsSync(player);
                    Thread.sleep(50);
                    sendAddPacketsSync(player);
                } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | IllegalArgumentException | IllegalAccessException | InvocationTargetException | InterruptedException ex) {
                    Logger.getLogger(NickedPlayer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    public void sendRemovePackets() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    try {
                        sendRemovePacketsSync(player);
                    } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        Logger.getLogger(NickedPlayer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
    }

    public void sendRemovePackets(final Player player) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    sendRemovePacketsSync(player);
                } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    Logger.getLogger(NickedPlayer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    public final void sendAddPackets() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    try {
                        sendAddPacketsSync(player);
                    } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                        Logger.getLogger(NickedPlayer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }).start();
    }

    public final void sendAddPackets(final Player player) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    sendAddPacketsSync(player);
                } catch (ClassNotFoundException | NoSuchMethodException | NoSuchFieldException | IllegalAccessException | IllegalArgumentException | InvocationTargetException ex) {
                    Logger.getLogger(NickedPlayer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }).start();
    }

    private void sendRemovePacketsSync(Player player) throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (player.getName().equalsIgnoreCase(getName())) {
            return;
        }
        Method getHandleMethod = getBukkitClass("entity.CraftPlayer").getMethod("getHandle");
        Field playerConncetionField = getNmsClass("EntityPlayer").getField("playerConnection");
        Method sendPacketMethod = getNmsClass("PlayerConnection").getMethod("sendPacket", getNmsClass("Packet"));

        Object entPlayer = getHandleMethod.invoke(player);
        Object playerConnection = playerConncetionField.get(entPlayer);
        sendPacketMethod.invoke(playerConnection, packetPlayOutEntityDestroy);
        sendPacketMethod.invoke(playerConnection, removeProfilePacket);
    }

    private void sendAddPacketsSync(Player player) throws ClassNotFoundException, NoSuchMethodException, NoSuchFieldException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
        if (player.getName().equalsIgnoreCase(getName())) {
            return;
        }
        Method getHandleMethod = getBukkitClass("entity.CraftPlayer").getMethod("getHandle");
        Field playerConncetionField = getNmsClass("EntityPlayer").getField("playerConnection");
        Method sendPacketMethod = getNmsClass("PlayerConnection").getMethod("sendPacket", getNmsClass("Packet"));

        Object entPlayer = getHandleMethod.invoke(player);
        Object playerConnection = playerConncetionField.get(entPlayer);
        sendPacketMethod.invoke(playerConnection, addProfilePacket);
        sendPacketMethod.invoke(playerConnection, packetPlayOutNamedEntitySpawn);
    }

    public String getName() {
        return name;
    }

    public UUID getUniqueID() {
        return uniqueID;
    }

    public String getNick() {
        return nick;
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(getName());
    }

    public void setNick(String nick) {
        this.nick = nick;
    }

    private String getServerVersion() {
        return Bukkit.getServer().getClass().getPackage().getName().substring(23);
    }

    private Class<?> getNmsClass(String nmsClassName) throws ClassNotFoundException {
        return Class.forName("net.minecraft.server." + getServerVersion() + "." + nmsClassName);
    }

    private Class<?> getBukkitClass(String bClassName) throws ClassNotFoundException {
        return Class.forName("org.bukkit.craftbukkit." + getServerVersion() + "." + bClassName);
    }

    private Field getField(Class<?> clazz, String name) {
        try {
            Field field = clazz.getDeclaredField(name);
            field.setAccessible(true);

            if (Modifier.isFinal(field.getModifiers())) {
                getField(Field.class, "modifiers").set(field, field.getModifiers() & ~Modifier.FINAL);
            }

            return field;
        } catch (IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException ex) {
            Logger.getLogger(NickedPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
