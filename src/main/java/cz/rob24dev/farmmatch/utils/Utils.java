package cz.rob24dev.farmmatch.utils;

import cz.rob24dev.farmmatch.Main;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class Utils {
    public static String getColorUtil(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static String getMessage(String message) {
        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Main.getInstance().getConfig().getString(message)));
    }

    public static String getPrefix() {
        return net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(Main.getInstance().getConfig().getString("messages.prefix")));
    }

    public static int getTimeToNextEvent() {
        int eventHour = Integer.parseInt(Utils.getMessage("eventHour"));
        LocalTime currentTime = LocalTime.now();
        int hour = currentTime.getHour();
        int nextEventHour = hour + (eventHour - hour % eventHour);
        if (nextEventHour == 24) {
            return Math.toIntExact(currentTime.until(LocalTime.of(23, 59, 59, 999999999), ChronoUnit.SECONDS) + 1L);
        }
        return Math.toIntExact(currentTime.until(LocalTime.of(nextEventHour, 0), ChronoUnit.SECONDS) + 1L);
    }

    public static void sendCenteredMessage(Player player, String message) {
        if (message == null || message.isEmpty()) {
            player.sendMessage("");
        }
        assert (message != null);
        message = net.md_5.bungee.api.ChatColor.translateAlternateColorCodes('&', message);
        int messagePxSize = 0;
        boolean previousCode = false;
        boolean isBold = false;
        for (char c : message.toCharArray()) {
            if (c == '§') {
                previousCode = true;
                continue;
            }
            if (previousCode) {
                previousCode = false;
                isBold = c == 'l' || c == 'L';
                continue;
            }
            DefaultFontInfo dFI = DefaultFontInfo.getDefaultFontInfo(c);
            messagePxSize += isBold ? dFI.getBoldLength() : dFI.getLength();
            ++messagePxSize;
        }
        int toCompensate = 154 - messagePxSize / 2;
        int spaceLength = DefaultFontInfo.SPACE.getLength() + 1;
        StringBuilder sb = new StringBuilder();
        for (int compensated = 0; compensated < toCompensate; compensated += spaceLength) {
            sb.append(" ");
        }
        player.sendMessage(sb + message);
    }
}

