package cz.rob24dev.farmmatch;

import cz.rob24dev.farmmatch.commands.Commands;
import cz.rob24dev.farmmatch.listeners.Listeners;
import cz.rob24dev.farmmatch.managers.Event;
import cz.rob24dev.farmmatch.managers.Farmer;
import cz.rob24dev.farmmatch.tasks.WaitingTask;
import cz.rob24dev.farmmatch.utils.Utils;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main
extends JavaPlugin {
    private static Main instance;
    public static HashMap<UUID, Farmer> farmerData;
    public static Event event;
    public static List<Location> boneMealUsedLocations;
    public static List<Location> placedBlocks;
    public void onEnable() {
        instance = this;
        farmerData = new HashMap<>();
        this.getServer().getPluginManager().registerEvents(new Listeners(),this);
        Objects.requireNonNull(this.getCommand("fm")).setExecutor(new Commands());
        Objects.requireNonNull(this.getCommand("fm")).setTabCompleter(new Commands());
        this.getConfig().options().configuration().options().copyDefaults(true);
        File configFile = new File(this.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            this.saveDefaultConfig();
        }
        Bukkit.getConsoleSender().sendMessage(Utils.getColorUtil("&6&lFarm&7&lMatch&8 (&61.0&8) &7has been &asuccessfully&7 &n&lenabled!&7 \n&7Plugin by: &6&lRob24Dev&7 \n&7 \n"));
        Main.startWaitingTask();
    }

    public void onDisable() {
        farmerData.clear();
        boneMealUsedLocations.clear();
        placedBlocks.clear();
        Bukkit.getConsoleSender().sendMessage(Utils.getColorUtil("&6&lFarm&7&lMatch&8 (&61.0&8) &7has been &asuccessfully&7 &c&ldisabled!&7 \n&7Plugin by: &6&lRob24Dev&7 \n&7 \n"));
    }

    public static Main getInstance() {
        return instance;
    }

    public static void startWaitingTask() {
        new WaitingTask().runTaskLater(Main.getInstance(), (long)Utils.getTimeToNextEvent() * 20L);
    }

    static {
        boneMealUsedLocations = new ArrayList<>();
        placedBlocks = new ArrayList<>();
    }
}

