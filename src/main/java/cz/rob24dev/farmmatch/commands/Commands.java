package cz.rob24dev.farmmatch.commands;

import cz.rob24dev.farmmatch.Main;
import cz.rob24dev.farmmatch.managers.Event;
import cz.rob24dev.farmmatch.managers.Farmer;
import cz.rob24dev.farmmatch.tasks.EventTask;
import cz.rob24dev.farmmatch.utils.Utils;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Commands implements CommandExecutor, TabCompleter {
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
        if (sender instanceof ConsoleCommandSender) {
            return false;
        }
        Player player = (Player)sender;
        if (label.equalsIgnoreCase("fm")) {
            if (args.length == 0) {
                if (player.hasPermission("fm.admin.maincommand")) {
                    player.sendMessage(Utils.getColorUtil("\n&6▉▉▉" +
                            "▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉ \n&6▉      &6Farm&7Match &7v.&61.0 \n&6▉           &8&nCommands&7 \n&6▉ \n&6▉ &7 &n/fm reload&7 \n&6▉ &7 Permission &6hbw.admin.reload \n&6▉ \n&6▉ &7 &n/fm event start&7 &6<collect/plant> <time> <item id> \n&6▉ &7 Permission &6hbw.event.start \n&6▉ \n&6▉ &7&n /fm event stop&7 \n&6▉ &7 Permission &6hbw.event.stop \n&6▉&6▉ &7&n /fm time&7 \n&6▉ &7 Permission &6X \n&6▉&6▉ &7&n /fm info&7 \n&6▉ &7 Permission &6X \n&6▉\n&6▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉"));
                } else {
                    player.sendMessage(Utils.getMessage("messages.noPermission").replace("%prefix%", Utils.getPrefix()));
                }
            } else if (args[0].equalsIgnoreCase("time")) {
                if (Main.event == null) {
                    if (Bukkit.getOnlinePlayers().size() >= Main.getInstance().getConfig().getInt("minPlayersStart")) {
                        int timeToEvent = Utils.getTimeToNextEvent();
                        int residue = timeToEvent % 3600;
                        String remainingTimeToEventFormat = Utils.getMessage("messages.timeCommand.remainingTimeToEventFormat").replace("%hodiny%", String.valueOf(timeToEvent / 3600)).replace("%minuty%", String.valueOf(residue / 60)).replace("%sekundy%", String.valueOf(residue % 60));
                        player.sendMessage(Utils.getMessage("messages.timeCommand.message").replace("%prefix%", Utils.getPrefix()).replace("%remainingTimeToEvent%", remainingTimeToEventFormat));
                    } else {
                        player.sendMessage(Utils.getMessage("messages.timeCommand.littlePlayers").replace("%prefix%", Utils.getPrefix()));
                    }
                } else {
                    player.sendMessage(Utils.getMessage("messages.timeCommand.eventAlreadyExists").replace("%prefix%", Utils.getPrefix()));
                }
            } else if (args[0].equalsIgnoreCase("info")) {
                if (Main.event != null) {
                    int remainingTime = EventTask.getEventRemainingTime();
                    String remainingTimeFormat = Utils.getMessage("messages.remainingEventTimeFormat").replace("%minuty%", String.valueOf(remainingTime / 60)).replace("%sekundy%", String.valueOf((remainingTime - 1) % 60));
                    List<String> eventItemsNames = Main.getInstance().getConfig().getStringList(Main.event.getEventType() + "ItemsNames");
                    List<String> infoListMessage = Main.getInstance().getConfig().getStringList("messages.infoCommand.message." + Main.event.getEventType() + "Event").stream().map(s -> s.replace("%prefix%", Utils.getPrefix())).map(s -> s.replace("%eItemName%", eventItemsNames.get(Main.event.getEventItem()))).map(s -> s.replace("%remainingTime%", remainingTimeFormat)).collect(Collectors.toList());
                    Farmer farmer = Main.farmerData.get(player.getUniqueId());
                    infoListMessage = Event.getReplacedPlaces(infoListMessage, Main.event.getEventType(), false);
                    for (String line : infoListMessage) {
                        if (line.contains("%center%")) {
                            line = line.replace("%center%", "");
                            String finalLine = Utils.getColorUtil(line).replace("%pos%", String.valueOf(Farmer.getPosition(farmer))).replace("%value%", String.valueOf(farmer.getCount()));
                            Utils.sendCenteredMessage(player, finalLine);
                            continue;
                        }
                        player.sendMessage(Utils.getColorUtil(line.replace("%pos%", String.valueOf(Farmer.getPosition(farmer))).replace("%value%", String.valueOf(farmer.getCount()))));
                    }
                } else {
                    player.sendMessage(Utils.getMessage("messages.infoCommand.eventNotExists").replace("%prefix%", Utils.getPrefix()));
                }
            } else if (args[0].equalsIgnoreCase("reload")) {
                if (player.hasPermission("fm.admin.reload")) {
                    if (Main.event != null) {
                        Event.finishEvent(true);
                        Bukkit.getOnlinePlayers().forEach(players -> players.sendMessage(Utils.getMessage("messages.adminStopEvent").replace("%prefix%", Utils.getPrefix())));
                    }
                    player.sendMessage(Utils.getMessage("messages.reloadCommand.reloading").replace("%prefix%", Utils.getPrefix()));
                    Main.getInstance().reloadConfig();
                    Main.getInstance().saveConfig();
                    player.sendMessage(Utils.getMessage("messages.reloadCommand.reloaded").replace("%prefix%", Utils.getPrefix()));
                } else {
                    player.sendMessage(Utils.getMessage("messages.noPermission").replace("%prefix%", Utils.getPrefix()));
                }
            } else if (args[0].equalsIgnoreCase("event")) {
                if (player.hasPermission("fm.event.start") | player.hasPermission("fm.event.stop")) {
                    if (args.length >= 2) {
                        if (args[1].equalsIgnoreCase("start")) {
                            if (player.hasPermission("fm.event.start")) {
                                if (args.length == 5) {
                                    if (Main.event == null) {
                                        boolean isNumeric;
                                        String type = args[2];
                                        try {
                                            Integer.parseInt(args[3]);
                                            isNumeric = true;
                                        } catch (NumberFormatException e) {
                                            isNumeric = false;
                                        }
                                        if (isNumeric) {
                                            int time = Integer.parseInt(args[3]);
                                            boolean collectEnable = Main.getInstance().getConfig().getBoolean("events.collect");
                                            boolean plantEnable = Main.getInstance().getConfig().getBoolean("events.plant");
                                            if (time != 0) {
                                                if (type.equalsIgnoreCase("collect")) {
                                                    if (collectEnable) {
                                                        String eventMaterial = args[4];
                                                        List<String> collectEventItems = Main.getInstance().getConfig().getStringList("collectEventItems");
                                                        if (collectEventItems.contains(eventMaterial)) {
                                                            Event.startEvent(type, time, Material.getMaterial(eventMaterial));
                                                            player.sendMessage(Utils.getColorUtil("%prefix% &7Collect event &6úspěšně začal.").replace("%prefix%", Utils.getPrefix()));
                                                        } else {
                                                            player.sendMessage(Utils.getColorUtil("%prefix% &7Item, který si zadal není v configu. &6Mrkni do configu collectEventItems").replace("%prefix%", Utils.getPrefix()));
                                                        }
                                                    } else {
                                                        player.sendMessage(Utils.getColorUtil("%prefix% &7Tento event je v &6configu zakázan.").replace("%prefix%", Utils.getPrefix()));
                                                    }
                                                } else if (type.equalsIgnoreCase("plant")) {
                                                    if (plantEnable) {
                                                        String eventMaterial = args[4];
                                                        List<String> plantEventItems = Main.getInstance().getConfig().getStringList("plantEventItems");
                                                        if (plantEventItems.contains(eventMaterial)) {
                                                            Event.startEvent(type, time, Material.getMaterial(eventMaterial));
                                                            player.sendMessage(Utils.getColorUtil("%prefix% &7Plant event &6úspěšně začal.").replace("%prefix%", Utils.getPrefix()));
                                                        } else {
                                                            player.sendMessage(Utils.getColorUtil("%prefix% &7Item, který si zadal není v configu. &6Mrkni do configu collectEventItems").replace("%prefix%", Utils.getPrefix()));
                                                        }
                                                    } else {
                                                        player.sendMessage(Utils.getColorUtil("%prefix% &7Tento event je v &6configu zakázan.").replace("%prefix%", Utils.getPrefix()));
                                                    }
                                                } else {
                                                    player.sendMessage(Utils.getColorUtil("%prefix% &7Typ události musí &6být collect,plant.").replace("%prefix%", Utils.getPrefix()));
                                                }
                                            } else {
                                                player.sendMessage(Utils.getColorUtil("%prefix% &7Čas eventu musí být &6číslo a nesmí být nula!").replace("%prefix%", Utils.getPrefix()));
                                            }
                                        } else {
                                            player.sendMessage(Utils.getColorUtil("%prefix% &7Čas eventu musí být &6číslo a nesmí být nula!").replace("%prefix%", Utils.getPrefix()));
                                        }
                                    } else {
                                        player.sendMessage(Utils.getColorUtil("%prefix% &7Nějaký event se &6již odehrává!").replace("%prefix%", Utils.getPrefix()));
                                    }
                                } else {
                                    player.sendMessage(Utils.getColorUtil("\n&6▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉ \n&6▉      &6Farm&7Match &7v.&61.0 \n&6▉           &8&nEvent Commands&7 \n&6▉ \n&6▉ &7 &n/fm event start&7 &6<collect/plant> <time> <item id> \n&6▉ &7 Permission &6hbw.event.start \n&6▉ \n&6▉ &7&n /fm event stop&7 \n&6▉ &7 Permission &6hbw.event.stop \n&6▉\n&6▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉"));
                                }
                            } else {
                                player.sendMessage(Utils.getMessage("messages.noPermission").replace("%prefix%", Utils.getPrefix()));
                            }
                        } else if (args[1].equalsIgnoreCase("stop")) {
                            if (player.hasPermission("fm.event.stop")) {
                                if (Main.event != null) {
                                    Event.finishEvent(true);
                                    player.sendMessage(Utils.getColorUtil("%prefix% &7Event byl &6úspěšně stopnut!").replace("%prefix%", Utils.getPrefix()));
                                    Bukkit.getOnlinePlayers().forEach(players -> players.sendMessage(Utils.getMessage("messages.adminStopEvent").replace("%prefix%", Utils.getPrefix())));
                                } else {
                                    player.sendMessage(Utils.getColorUtil("%prefix% &7Momentálně žádný &6event neprobíhá!").replace("%prefix%", Utils.getPrefix()));
                                }
                            } else {
                                player.sendMessage(Utils.getMessage("messages.noPermission").replace("%prefix%", Utils.getPrefix()));
                            }
                        } else {
                            player.sendMessage(Utils.getColorUtil("\n&6▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉ \n&6▉      &6Farm&7Match &7v.&61.0 \n&6▉           &8&nEvent Commands&7 \n&6▉ \n&6▉ &7 &n/fm event start&7 &6<collect/plant> <time> <item id> \n&6▉ &7 Permission &6hbw.event.start \n&6▉ \n&6▉ &7&n /fm event stop&7 \n&6▉ &7 Permission &6hbw.event.stop \n&6▉\n&6▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉"));
                        }
                    } else {
                        player.sendMessage(Utils.getColorUtil("\n&6▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉ \n&6▉      &6Farm&7Match &7v.&61.0 \n&6▉           &8&nEvent Commands&7 \n&6▉ \n&6▉ &7 &n/fm event start&7 &6<collect/plant> <time> <item id> \n&6▉ &7 Permission &6hbw.event.start \n&6▉ \n&6▉ &7&n /fm event stop&7 \n&6▉ &7 Permission &6hbw.event.stop \n&6▉\n&6▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉"));
                    }
                } else {
                    player.sendMessage(Utils.getMessage("messages.noPermission").replace("%prefix%", Utils.getPrefix()));
                }
            } else if (player.hasPermission("fm.showcommands")) {
                player.sendMessage(Utils.getColorUtil("\n&6▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉ \n&6▉      &6Farm&7Match &7v.&61.0 bv\n&6▉           &8&nEvent Commands&7 \n&6▉ \n&6▉ &7 &n/fm event start&7 &6<collect/plant> <time> <item id> \n&6▉ &7 Permission &6hbw.event.start \n&6▉ \n&6▉ &7&n /fm event stop&7 \n&6▉ &7 Permission &6hbw.event.stop \n&6▉\n&6▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉▉"));
            } else {
                player.sendMessage(Utils.getMessage("messages.noPermission").replace("%prefix%", Utils.getPrefix()));
            }
        }
        return false;
    }

    public List<String> onTabComplete(CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        if (sender.hasPermission("fm.showcommands")) {
            if (args.length == 1) {
                return Arrays.asList("reload", "event", "time", "info");
            }
            if (args.length >= 2 && args[0].equalsIgnoreCase("event")) {
                if (args.length > 2) {
                    if (args[1].equalsIgnoreCase("start")) {
                        if (args.length == 3) {
                            return Arrays.asList("collect", "plant");
                        }
                        if (args.length == 4) {
                            return Collections.singletonList("10");
                        }
                        if (args.length == 5) {
                            if (args[2].equalsIgnoreCase("collect")) {
                                return Main.getInstance().getConfig().getStringList("collectEventItems");
                            }
                            if (args[2].equalsIgnoreCase("plant")) {
                                return Main.getInstance().getConfig().getStringList("plantEventItems");
                            }
                        }
                    }
                } else {
                    return Arrays.asList("start", "stop");
                }
            }
            return null;
        }
        if (args.length == 1) {
            return Arrays.asList("time", "info");
        }
        return null;
    }
}

