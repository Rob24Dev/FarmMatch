package cz.rob24dev.farmmatch.tasks;

import cz.rob24dev.farmmatch.Main;
import cz.rob24dev.farmmatch.managers.Event;
import cz.rob24dev.farmmatch.managers.Farmer;
import cz.rob24dev.farmmatch.utils.Utils;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

public class EventTask extends BukkitRunnable {
    public static int time;

    public void run() {
        if (--time > 0) {
            if (Bukkit.getOnlinePlayers().isEmpty()) {
                Event.finishEvent(false);
                this.cancel();
            } else if (Main.event != null) {
                int firstTime = Main.event.getEventTime() * 60;
                Main.event.getEventBossBar().setProgress(Math.max(Main.event.getEventBossBar().getProgress() - 1.0 / (double) firstTime, 0.0));
                Main.event.getEventBossBar().setTitle(Utils.getMessage("messages.eventStart.bossBar." + Main.event.getEventType() + "Event.title").replace("%remainingTime%", Utils.getMessage("messages.remainingEventTimeFormat").replace("%minuty%", String.valueOf((time - 1) / 60))).replace("%sekundy%", String.valueOf((time - 1) % 60)));
                if (firstTime / 2 == time) {
                    List<String> inProgessEventListMessage = Main.getInstance().getConfig().getStringList("messages.eventInProgress.message." + Main.event.getEventType() + "Event");
                    inProgessEventListMessage = Event.getReplacedPlaces(inProgessEventListMessage, Main.event.getEventType(), false);
                    for (Player player : Bukkit.getOnlinePlayers()) {
                        Farmer farmer = Main.farmerData.get(player.getUniqueId());
                        for (String line : inProgessEventListMessage) {
                            line = farmer.getCount() == 1 ? line.replace("%CzechReplace%", "em") : line.replace("%CzechReplace%", "y");
                            if (line.contains("%center%")) {
                                line = line.replace("%center%", "");
                                String finalLine = Utils.getColorUtil(line);
                                Utils.sendCenteredMessage(player, finalLine.replace("%pos%", String.valueOf(Farmer.getPosition(farmer))).replace("%value%", String.valueOf(farmer.getCount())));
                                continue;
                            }
                            player.sendMessage(Utils.getColorUtil(line).replace("%pos%", String.valueOf(Farmer.getPosition(farmer))).replace("%value%", String.valueOf(farmer.getCount())));
                        }
                    }
                }
            } else {
                this.cancel();
            }
        } else {
            Event.finishEvent(true);
            this.cancel();
        }
    }

    public static int getEventRemainingTime() {
        return time;
    }
}

