package cz.rob24dev.farmmatch.managers;

import cz.rob24dev.farmmatch.Main;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class Farmer {
    Player player;
    int count;
    List<Location> plantedLocs;

    public Farmer(Player player, int count, List<Location> plantedLocs) {
        this.player = player;
        this.count = count;
        this.plantedLocs = plantedLocs;
    }

    public Player getPlayer() {
        return this.player;
    }

    public int getCount() {
        return this.count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public List<Location> getPlantedLocs() {
        return this.plantedLocs;
    }

    public static List<Farmer> getFarmersList() {
        return new ArrayList<>(Main.farmerData.values());
    }

    public static int getPosition(Farmer farmer) {
        List<Farmer> farmers = Farmer.getFarmersList();
        farmers.sort((f1, f2) -> Integer.compare(f2.getCount(), f1.getCount()));
        return farmers.indexOf(farmer) + 1;
    }
}

