package cz.rob24dev.farmmatch.tasks;

import cz.rob24dev.farmmatch.Main;
import cz.rob24dev.farmmatch.managers.Event;
import cz.rob24dev.farmmatch.utils.Utils;
import java.util.Calendar;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;

public class WaitingTask extends BukkitRunnable {

    public void run() {
        Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR);
        if (hour % Integer.parseInt(Utils.getMessage("eventHour")) == 0 && Bukkit.getOnlinePlayers().size() >= Main.getInstance().getConfig().getInt("minPlayersStart")) {
            Event.startEvent(null, 0, null);
        }
        this.cancel();
    }
}

