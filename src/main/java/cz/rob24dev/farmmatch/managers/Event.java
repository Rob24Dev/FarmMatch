package cz.rob24dev.farmmatch.managers;

import cz.rob24dev.farmmatch.Main;
import cz.rob24dev.farmmatch.tasks.EventTask;
import cz.rob24dev.farmmatch.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;

public class Event {
    String eventType;
    int eventTime;
    int eventItem;
    BossBar eventBossBar;

    public Event(String eventType, int eventTime, int eventItem, BossBar eventBossBar) {
        this.eventType = eventType;
        this.eventTime = eventTime;
        this.eventItem = eventItem;
        this.eventBossBar = eventBossBar;
    }

    public BossBar getEventBossBar() {
        return this.eventBossBar;
    }

    public int getEventTime() {
        return this.eventTime;
    }

    public int getEventItem() {
        return this.eventItem;
    }

    public String getEventType() {
        return this.eventType;
    }

    public static void startEvent(String eventType, int eventTime, Material eventItem) {
        int eventItemIndex;
        List<String> collectEventItems = Main.getInstance().getConfig().getStringList("collectEventItems");
        List<String> plantEventItems = Main.getInstance().getConfig().getStringList("plantEventItems");
        if (eventItem == null) {
            Random r = new Random();
            boolean collectEnable = Main.getInstance().getConfig().getBoolean("events.collect");
            boolean plantEnable = Main.getInstance().getConfig().getBoolean("events.plant");
            if (!collectEnable) {
                eventType = "plant";
            } else if (!plantEnable) {
                eventType = "collect";
            } else {
                String[] eventTypes = new String[]{"collect", "plant"};
                eventType = eventTypes[r.nextInt(2)];
            }
            int minEventTime = Main.getInstance().getConfig().getInt("minTime");
            int maxEventTime = Main.getInstance().getConfig().getInt("maxTime");
            eventTime = r.nextInt(maxEventTime - minEventTime + 1) + minEventTime;
            if (eventType.equalsIgnoreCase("collect")) {
                eventItemIndex = r.nextInt(collectEventItems.size());
                eventItem = Material.getMaterial(String.valueOf((collectEventItems.get(eventItemIndex))));
            } else {
                eventItemIndex = r.nextInt(plantEventItems.size());
                eventItem = Material.getMaterial(String.valueOf((plantEventItems.get(eventItemIndex))));
            }
        } else {
            eventItemIndex = Main.getInstance().getConfig().getStringList(eventType + "EventItems").indexOf(eventItem.toString());
        }
        BossBar bossBar = Bukkit.createBossBar(Utils.getMessage("messages.eventStart.bossBar." + eventType + "Event.title"), BarColor.valueOf(Utils.getMessage("messages.eventStart.bossBar." + eventType + "Event.color")), BarStyle.valueOf(Utils.getMessage("messages.eventStart.bossBar." + eventType + "Event.style")));
        List<String> eventItemsNames = Main.getInstance().getConfig().getStringList(eventType + "ItemsNames");
        int finalEventTime = eventTime;
        List<String> startListMessage = Main.getInstance().getConfig().getStringList("messages.eventStart.message." + eventType + "Event")
                .stream().map(s -> s.replace("%prefix%", Utils.getPrefix()))
                .map(s -> s.replace("%eItemName%", eventItemsNames.get(eventItemIndex)))
                .map(s -> s.replace("%time%", Integer.toString(finalEventTime))).collect(Collectors.toList());
        bossBar.setVisible(true);
        bossBar.setProgress(1.0);
        for (Player player : Bukkit.getOnlinePlayers()) {
            Event.sendListOfMessage(startListMessage, player);
            Main.farmerData.put(player.getUniqueId(), new Farmer(player, 0, new ArrayList<>()));
            bossBar.addPlayer(player);
        }
        Main.event = new Event(eventType, eventTime, eventItemIndex, bossBar);
        EventTask eventTask = new EventTask();
        EventTask.time = Main.event.getEventTime() * 60 + 1;
        eventTask.runTaskTimer(Main.getInstance(), 0L, 20L);
    }

    private static void sendListOfMessage(List<String> list, Player player) {
        for (String line : list) {
            if (line.contains("%center%")) {
                line = line.replace("%center%", "");
                String finalLine = Utils.getColorUtil(line);
                Utils.sendCenteredMessage(player, finalLine);
                continue;
            }
            player.sendMessage(Utils.getColorUtil(line));
        }
    }

    public static void finishEvent(boolean isNormalStop) {
        if (isNormalStop) {
            List<String> rewardsForPlaying = Main.getInstance().getConfig().getStringList("messages.eventEnd.reward.rewardForPlaying");
            List<String> finishEventListMessage = Main.getInstance().getConfig().getStringList("messages.eventEnd.message." + Main.event.getEventType() + "Event");
            finishEventListMessage = Event.getReplacedPlaces(finishEventListMessage, Main.event.getEventType(), true);
            for (Player players : Bukkit.getOnlinePlayers()) {
                Farmer farmer = Main.farmerData.get(players.getUniqueId());
                if (farmer.getCount() > 0 & Farmer.getPosition(farmer) > 3) {
                    for (String reward : rewardsForPlaying) {
                        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), reward.replace("%player%", players.getName()));
                    }
                    players.sendMessage(Utils.getMessage("messages.eventEnd.playerCollectedRewardMessage.rewardForPlaying").replace("%prefix%", Utils.getPrefix()));
                }
                Event.sendListOfMessage(finishEventListMessage, players);
            }
        }
        Main.event.getEventBossBar().removeAll();
        Main.event = null;
        Main.farmerData.clear();
        Main.boneMealUsedLocations.clear();
        Main.startWaitingTask();
    }

    public static List<String> getReplacedPlaces(List<String> listMessage, String eventType, boolean isFinishMessage) {
        Player thirdPlayer;
        Player secondPlayer;
        Player firstPlayer;
        List<Farmer> farmers = Farmer.getFarmersList();
        farmers.sort((f1, f2) -> Integer.compare(f2.getCount(), f1.getCount()));
        String eventValuePlaceholder = eventType.equalsIgnoreCase("plant") ? "Planted%" : "Collected%";
        if (!isFinishMessage) {
            List<String> eventItemsNames = Main.getInstance().getConfig().getStringList(eventType + "ItemsNames");
            listMessage = listMessage.stream().map(s -> s.replace("%prefix%", Utils.getPrefix())).map(s -> s.replace("%eItemName%", eventItemsNames.get(Main.event.getEventItem()))).collect(Collectors.toList());
        } else {
            listMessage = listMessage.stream().map(s -> s.replace("%prefix%", Utils.getPrefix())).collect(Collectors.toList());
        }
        if (farmers.get(0).getCount() != 0) {
            firstPlayer = farmers.get(0).getPlayer();
            listMessage = listMessage.stream().map(s -> s.replace("%1PlayerName%", firstPlayer.getName())).map(s -> s.replace("%1" + eventValuePlaceholder, String.valueOf(farmers.get(0).getCount()))).collect(Collectors.toList());
        } else {
            firstPlayer = null;
            listMessage = listMessage.stream().map(s -> s.replace("%1PlayerName%", "N/A")).map(s -> s.replace("%1" + eventValuePlaceholder, "N/A")).collect(Collectors.toList());
        }
        if (2 <= farmers.size() && farmers.get(1).getCount() != 0) {
            secondPlayer = farmers.get(1).getPlayer();
            listMessage = listMessage.stream().map(s -> s.replace("%2PlayerName%", secondPlayer.getName())).map(s -> s.replace("%2" + eventValuePlaceholder, String.valueOf(farmers.get(1).getCount()))).collect(Collectors.toList());
        } else {
            secondPlayer = null;
            listMessage = listMessage.stream().map(s -> s.replace("%2PlayerName%", "N/A")).map(s -> s.replace("%2" + eventValuePlaceholder, "N/A")).collect(Collectors.toList());
        }
        if (3 <= farmers.size() && farmers.get(2).getCount() != 0) {
            thirdPlayer = farmers.get(2).getPlayer();
            listMessage = listMessage.stream().map(s -> s.replace("%3PlayerName%", thirdPlayer.getName())).map(s -> s.replace("%3" + eventValuePlaceholder, String.valueOf((farmers.get(2)).getCount()))).collect(Collectors.toList());
        } else {
            thirdPlayer = null;
            listMessage = listMessage.stream().map(s -> s.replace("%3PlayerName%", "N/A")).map(s -> s.replace("%3" + eventValuePlaceholder, "N/A")).collect(Collectors.toList());
        }
        if (isFinishMessage) {
            List<String> placeReward;
            if (farmers.get(0).getCount() != 0) {
                placeReward = Main.getInstance().getConfig().getStringList("messages.eventEnd.reward.1");
                for (String rewardCommand : placeReward) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), rewardCommand.replace("%player%", Objects.requireNonNull(firstPlayer).getName()));
                }
                Objects.requireNonNull(firstPlayer).sendMessage(Utils.getMessage("messages.eventEnd.playerCollectedRewardMessage.1").replace("%prefix%", Utils.getPrefix()));
            }
            if (2 <= farmers.size() && farmers.get(1).getCount() != 0) {
                placeReward = Main.getInstance().getConfig().getStringList("messages.eventEnd.reward.2");
                for (String rewardCommand : placeReward) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), rewardCommand.replace("%player%", Objects.requireNonNull(secondPlayer).getName()));
                }
                Objects.requireNonNull(secondPlayer).sendMessage(Utils.getMessage("messages.eventEnd.playerCollectedRewardMessage.2").replace("%prefix%", Utils.getPrefix()));
            }
            if (3 <= farmers.size() && farmers.get(2).getCount() != 0) {
                placeReward = Main.getInstance().getConfig().getStringList("messages.eventEnd.reward.3");
                for (String rewardCommand : placeReward) {
                    Bukkit.dispatchCommand(Bukkit.getConsoleSender(), rewardCommand.replace("%player%", Objects.requireNonNull(thirdPlayer).getName()));
                }
                Objects.requireNonNull(thirdPlayer).sendMessage(Utils.getMessage("messages.eventEnd.playerCollectedRewardMessage.3").replace("%prefix%", Utils.getPrefix()));
            }
        }
        return listMessage;
    }
}

