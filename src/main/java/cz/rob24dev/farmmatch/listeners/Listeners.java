package cz.rob24dev.farmmatch.listeners;

import cz.rob24dev.farmmatch.Main;
import cz.rob24dev.farmmatch.managers.Farmer;
import cz.rob24dev.farmmatch.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class Listeners implements Listener {
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        if (Main.event != null) {
            Main.farmerData.put(player.getUniqueId(), new Farmer(player, 0, new ArrayList<>()));
            String joinInEventMessage = Utils.getMessage("messages.joinInEvent").replace("%prefix%", Utils.getPrefix()).replace("%time%", Integer.toString(Main.event.getEventTime()));
            List<String> eventItemsNames = Main.getInstance().getConfig().getStringList(Main.event.getEventType() + "ItemsNames");
            joinInEventMessage = Main.event.getEventType().equalsIgnoreCase("collect") ? joinInEventMessage.replace("%eventName%", "sesbírání").replace("%ItemName%", eventItemsNames.get(Main.event.getEventItem())) : joinInEventMessage.replace("%eventName%", "zasazení").replace("%ItemName%", eventItemsNames.get(Main.event.getEventItem()));
            Main.event.getEventBossBar().addPlayer(player);
            player.sendMessage(joinInEventMessage);
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (Main.event != null) {
            Main.farmerData.remove(player.getUniqueId());
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (Main.event == null || Main.event.getEventType().equals("plant")) {
            return;
        }
        Player player = event.getPlayer();
        Farmer farmer = Main.farmerData.get(player.getUniqueId());
        Block block = event.getBlock();
        List<String> collectItems = Main.getInstance().getConfig().getStringList("collectEventItems");
        if(Main.placedBlocks.contains(event.getBlock().getLocation())) return;
        if (block.getType().toString().contains(collectItems.get(Main.event.getEventItem()))) {
            if (!Main.boneMealUsedLocations.contains(event.getBlock().getLocation())) {
                if (block.getType().equals(Material.MELON) || block.getType().equals(Material.PUMPKIN)) {
                    farmer.setCount(farmer.getCount() + 1);
                    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Utils.getMessage("messages.progressActionBar").replace("%prefix%", Utils.getPrefix()).replace("%amount%", String.valueOf(farmer.getCount()))));
                    this.sendProgressActionBar(player);
                } else {
                    Ageable ageable = (Ageable)event.getBlock().getBlockData();
                    if (ageable.getAge() == ageable.getMaximumAge()) {
                        farmer.setCount(farmer.getCount() + 1);
                        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Utils.getMessage("messages.progressActionBar").replace("%prefix%", Utils.getPrefix()).replace("%amount%", String.valueOf(farmer.getCount()))));
                        this.sendProgressActionBar(player);
                    }
                }
            } else {
                player.sendMessage(Utils.getMessage("messages.boneMealUse").replace("%prefix%", Utils.getPrefix()));
                Main.boneMealUsedLocations.remove(event.getBlock().getLocation());
            }
        }
    }

    private void sendProgressActionBar(Player player) {
        Farmer farmer = Main.farmerData.get(player.getUniqueId());
        if (farmer.getCount() == 1) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Utils.getMessage("messages.progressActionBar").replace("%prefix%", Utils.getPrefix()).replace("%amount%", String.valueOf(farmer.getCount())).replace("%CzechReplace%", "")));
        } else if (farmer.getCount() > 1 && farmer.getCount() < 5) {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Utils.getMessage("messages.progressActionBar").replace("%prefix%", Utils.getPrefix()).replace("%amount%", String.valueOf(farmer.getCount())).replace("%CzechReplace%", "y")));
        } else {
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(Utils.getMessage("messages.progressActionBar").replace("%prefix%", Utils.getPrefix()).replace("%amount%", String.valueOf(farmer.getCount())).replace("%CzechReplace%", "ů")));
        }
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Block block = event.getBlock();
        if(block.getType().equals(Material.MELON) | block.getType().equals(Material.PUMPKIN)) {
            Main.placedBlocks.add(block.getLocation());
        }
        if (Main.event == null || Main.event.getEventType().equals("collect")) {
            return;
        }
        Player player = event.getPlayer();
        Farmer farmer = Main.farmerData.get(player.getUniqueId());
        List<Location> plantedLocs = farmer.getPlantedLocs();
        List<String> plantItems = Main.getInstance().getConfig().getStringList("plantEventItems");
        if (block.getType().toString().contains(plantItems.get(Main.event.getEventItem()))) {
            if (plantedLocs.contains(block.getLocation())) {
                return;
            }
            plantedLocs.add(block.getLocation());
            farmer.setCount(farmer.getCount() + 1);
            this.sendProgressActionBar(player);
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent event) {
        if (Main.event == null || Main.event.getEventType().equals("plant")) {
            return;
        }
        Player player = event.getPlayer();
        if(event.getClickedBlock() == null) return;
        if(event.getClickedBlock().getType().equals(Material.NETHER_WART)) return;
        Material itemMaterial = player.getInventory().getItemInMainHand().getType();
        if (event.getAction().equals(Action.RIGHT_CLICK_BLOCK) && !Main.boneMealUsedLocations.contains(Objects.requireNonNull(event.getClickedBlock()).getLocation())) {
            List<String> collectItems = Main.getInstance().getConfig().getStringList("collectEventItems");
            if (event.getClickedBlock().getType().toString().contains(collectItems.get(Main.event.getEventItem()))) {
                if (itemMaterial == Material.BONE_MEAL) {
                    if (!event.getClickedBlock().getType().toString().contains("_STEM")) {
                        Ageable ageable = (Ageable)event.getClickedBlock().getBlockData();
                        if (ageable.getAge() != ageable.getMaximumAge()) {
                            Main.boneMealUsedLocations.add(event.getClickedBlock().getLocation());
                        }
                    } else {
                        Main.boneMealUsedLocations.add(event.getClickedBlock().getLocation());
                    }
                }
            } else if (event.getClickedBlock().getType().toString().contains("_STEM") && (collectItems.get(Main.event.getEventItem())).contains("_STEM") && itemMaterial == Material.BONE_MEAL) {
                Main.boneMealUsedLocations.add(event.getClickedBlock().getLocation());
            }
        }
    }

    @EventHandler
    public void onDispenserDispense(BlockDispenseEvent event) {
        if (Main.event == null || Main.event.getEventType().equals("plant")) {
            return;
        }
        if (event.getItem().getType() == Material.BONE_MEAL) {
            event.setCancelled(true);
        }
    }
}

