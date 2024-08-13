package com.magnesify.magnesifydungeons.events;

import com.magnesify.magnesifydungeons.MagnesifyDungeons;
import com.magnesify.magnesifydungeons.dungeon.Dungeon;
import com.magnesify.magnesifydungeons.dungeon.entitys.DungeonConsole;
import com.magnesify.magnesifydungeons.dungeon.entitys.DungeonPlayer;
import com.magnesify.magnesifydungeons.files.Options;
import com.magnesify.magnesifydungeons.genus.DungeonGenus;
import com.magnesify.magnesifydungeons.genus.gui.GenusGuiLoader;
import com.magnesify.magnesifydungeons.genus.gui.IAGenusGuiLoader;
import com.magnesify.magnesifydungeons.modules.managers.DatabaseManager;
import com.magnesify.magnesifydungeons.modules.Defaults;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Zombie;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import static com.magnesify.magnesifydungeons.MagnesifyDungeons.get;
import static com.magnesify.magnesifydungeons.dungeon.entitys.DungeonPlayer.parseHexColors;

public class DungeonPlayerEvents implements Listener {
    public DungeonPlayerEvents(MagnesifyDungeons magnesifyDungeons) {}

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        DungeonPlayer dungeonPlayer = new DungeonPlayer(event.getPlayer());
        dungeonPlayer.createDungeonAccount(event);
        Defaults defaults = new Defaults();
        if (Bukkit.getWorld(defaults.MainSpawn().world()) != null) {
            Location loc = new Location(Bukkit.getWorld(defaults.MainSpawn().world()), defaults.MainSpawn().x(), defaults.MainSpawn().y(), defaults.MainSpawn().z(), (float) defaults.MainSpawn().yaw(), (float) defaults.MainSpawn().pitch());
            event.getPlayer().teleport(loc);
        } else {
            DungeonConsole dungeonConsole = new DungeonConsole();
            dungeonConsole.ConsoleOutputManager().write("<#4f91fc>[Magnesify Dungeons] &fBaşlangıç henüz ayarlanmamış &d/mgd setmainspawn &fyazarak başlangıcı ayarlayabilirsin.");
        }
        DungeonGenus dungeonGenus = new DungeonGenus(event.getPlayer());
        Options options = new Options();
        if(options.get().getBoolean("options.open-genus-select-gui-if-genus-is-not-set")) {
            if(!dungeonGenus.isGenusSet()) {
                boolean textc = get().getConfig().isSet("settings.genus.custom-gui-texture");
                if(textc) {
                    if(Bukkit.getPluginManager().getPlugin("ItemsAdder") != null) {
                        IAGenusGuiLoader.openInventory(event.getPlayer());
                    } else {
                        Bukkit.getConsoleSender().sendMessage(parseHexColors("<#4b8eff>[Magnesify Dungeons] &f'settings.genus.custom-gui-texture' ayarlanmış durumda ancak ItemsAdder sunucuda bulunmuyor..."));
                        GenusGuiLoader.openInventory(event.getPlayer());
                    }
                } else {
                    GenusGuiLoader.openInventory(event.getPlayer());
                }
            }
        }
    }

    @EventHandler
    public void leave(PlayerQuitEvent event) {
        DungeonPlayer dungeonPlayer = new DungeonPlayer(event.getPlayer());
        if(dungeonPlayer.inDungeon()) {
            Dungeon dungeon = new Dungeon(get().getPlayers().getLastDungeon(event.getPlayer()));
            dungeon.status(true);
            dungeon.updateCurrentPlayer("Yok");
            get().getPlayers().setDone(event.getPlayer(), true);
            dungeon.events().stop(event.getPlayer());
            get().getPlayers().updateDungeonStatus(event.getPlayer(), false);
            get().getPlayers().updateDeath(event.getPlayer(), 1);
            killEntity(get().getPlayers().getLastBoss(event.getPlayer()));
        }
    }

    public void killEntity(String lastBoss) {
        for (Entity entity : Bukkit.getWorld(get().getConfig().getString("settings.defaults.dungeon-world")).getEntities()) {
            if (entity instanceof Zombie) {
                String key = entity.getMetadata("name").get(0).asString();
                String metadataValue = entity.getMetadata("dungeon").get(0).asString();
                Zombie zombie = (Zombie) entity;
                DatabaseManager databaseManager = new DatabaseManager(get());
                if(databaseManager.isDungeonExists(metadataValue) && databaseManager.getCurrentPlayer(metadataValue).equalsIgnoreCase("Yok")) {
                    if(metadataValue != null) {
                        if (lastBoss.equalsIgnoreCase(key)) {
                            if (entity.hasMetadata("name")) {
                                zombie.remove();
                            }
                        }
                    }
                }
            }
        }
    }

}
