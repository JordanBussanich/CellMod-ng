package com.jordanbussanich.cellmodng.commands;

import com.jordanbussanich.cellmodng.CellMod;

import java.sql.ResultSet;
import java.util.Collection;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SendCmd implements CommandExecutor {

    private final CellMod plugin;
    private CommandSender cSender;

    public SendCmd(CellMod instance) {
        plugin = instance;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        Player p = null;
        try {
            p = (Player) cs;
        } catch (Exception ex) {
            cs.sendMessage("Only players may use this command.");
            return true;
        }
        if (strings.length > 1) {
            if (plugin.hasPermission((Player) cs, "cellmod.use.send")) {
                if (plugin.getPlayerCarrier(p) != null) {
                    String text = "";
                    Integer temp = 0;
                    for (String s : strings) {
                        temp++;
                        if (temp > 1) {
                            text = text + s + " ";
                        }
                    }
                    Player messagefrom = (Player) cs;
                    Player messageto = null;
                    ResultSet getplayerfromnumber = plugin.getResult("SELECT * FROM players WHERE Number='" + strings[0] + "';");
                    try {
                        while (getplayerfromnumber.next()) {
                            messageto = plugin.getServer().getPlayer(getplayerfromnumber.getString("Player"));
                        }
                        if (messageto == null && strings[0].equals(plugin.mainConfig.getString("phone.specialnumbers.emergency"))) {
                            messagefrom.sendMessage(plugin.languageConfig.getString("Header"));
                            messagefrom.sendMessage(plugin.getPlayerCarrierName(messagefrom) + " " + plugin.getSignal(messagefrom, 0.0));
                            messagefrom.sendMessage(plugin.languageConfig.getString("InvaildDevice"));
                        } else {
                            if (plugin.getPlayerCarrier(messagefrom).getPlayerRemainingMessages(messagefrom) > 0 || (strings[0].equals(plugin.mainConfig.getString("phone.specialnumbers.emergency")) && plugin.mainConfig.getBoolean("phone.charge-emergency"))) {
                                if (!(strings[0].equals(plugin.mainConfig.getString("phone.specialnumbers.emergency")) && plugin.mainConfig.getBoolean("phone.charge-emergency"))) {
                                    Integer remaining = plugin.getPlayerCarrier(messagefrom).getPlayerRemainingMessages(messagefrom) - 1;
                                    plugin.getPlayerCarrier(messagefrom).setPlayerRemainingMessage(messagefrom, remaining);
                                }
                                sendText(messagefrom, messageto, strings[0], text);
                            } else {
                                messagefrom.sendMessage(ChatColor.RED + plugin.languageConfig.getString("NoMessagesPlan"));
                            }
                        }
                    } catch (Exception ex) {
                        System.out.println(ex.toString());
                    }
                } else {
                    cs.sendMessage(ChatColor.RED + plugin.languageConfig.getString("JoinNetwork"));
                }
            } else {
                cs.sendMessage(ChatColor.RED + plugin.languageConfig.getString("NoPermission"));
            }
        } else {
            cs.sendMessage(plugin.languageConfig.getString("WrongLength"));
        }
        return true;
    }

    public void sendText(Player from, Player to, String tonum, String text) {
        String noservice = plugin.languageConfig.getString("NoService") + " [    ]";
        if (to == null && tonum.equals(plugin.mainConfig.getString("phone.specialnumbers.emergency"))) {
            if (!noservice.equals(plugin.getSignal(from, 0.0)) || !plugin.mainConfig.getBoolean("phone.require-service-emergency")) {
                from.sendMessage(plugin.languageConfig.getString("Header"));
                from.sendMessage(plugin.getPlayerCarrierName(from) + " " + plugin.getSignal(from, 0.0));
                from.sendMessage(plugin.languageConfig.getString("MessageSent"));

                ResultSet fromnumber = plugin.getResult("SELECT * from players WHERE Player='" + from.getName() + "';");
                String fromnum = "";
                try {
                    while (fromnumber.next()) {
                        fromnum = fromnumber.getString("Number");
                    }
                } catch (Exception ex) {
                    fromnum = "0000";
                }

                Collection<? extends Player> players = plugin.getServer().getOnlinePlayers();

                for (Player p : players) {
                    if (plugin.hasPermission(p, "cellmod.emcontact")) {
                        p.sendMessage(ChatColor.RED + "[" + plugin.languageConfig.getString("EmTitle") + "] (" + from.getName() + " / " + fromnum + ") " + text);
                    }
                }
            } else {
                from.sendMessage(plugin.languageConfig.getString("Header"));
                from.sendMessage(plugin.getPlayerCarrierName(from) + " " + plugin.getSignal(from, 0.0));
                from.sendMessage(plugin.languageConfig.getString("ServiceRequired"));
            }
            return;
        }
        if (to.isOnline()) {
            if (!noservice.equals(plugin.getSignal(to, 0.0))) {
                if (!noservice.equals(plugin.getSignal(from, 0.0))) {
                    from.sendMessage(plugin.languageConfig.getString("Header"));
                    from.sendMessage(plugin.getPlayerCarrierName(from) + " " + plugin.getSignal(from, 0.0));
                    from.sendMessage(plugin.languageConfig.getString("MessageSent"));
                    to.sendMessage(plugin.languageConfig.getString("Header"));
                    to.sendMessage(plugin.getPlayerCarrierName(to) + " " + plugin.getSignal(to, 0.0));
                    ResultSet fromnumber = plugin.getResult("SELECT * from players WHERE Player='" + from.getName() + "';");
                    String fromnum = "";
                    try {
                        while (fromnumber.next()) {
                            fromnum = fromnumber.getString("Number");
                        }
                    } catch (Exception ex) {
                        fromnum = "0000";
                    }
                    to.sendMessage(plugin.languageConfig.getString("From") + ": " + from.getName() + " (" + fromnum + ")");
                    to.sendMessage(text);
                } else {
                    from.sendMessage(plugin.languageConfig.getString("Header"));
                    from.sendMessage(plugin.getPlayerCarrierName(from) + " " + plugin.getSignal(from, 0.0));
                    from.sendMessage(plugin.languageConfig.getString("ServiceRequired"));
                }
            } else {
                from.sendMessage(plugin.languageConfig.getString("Header"));
                from.sendMessage(plugin.getPlayerCarrierName(from) + " " + plugin.getSignal(from, 0.0));
                from.sendMessage(plugin.languageConfig.getString("MessageSent"));
                plugin.runInsertQuery("INSERT INTO backlog (FromUser, ToUser, MessageText) VALUES ('" + from.getName() + "', '" + to.getName() + "', '" + text + "');");
            }
        } else {
            from.sendMessage(plugin.languageConfig.getString("Header"));
            from.sendMessage(plugin.getPlayerCarrierName(from) + " " + plugin.getSignal(from, 0.0));
            from.sendMessage(plugin.languageConfig.getString("MessageSent"));
            plugin.runInsertQuery("INSERT INTO backlog (FromUser, ToUser, MessageText) VALUES ('" + from.getName() + "', '" + to.getName() + "', '" + text + "');");
        }
    }
}
