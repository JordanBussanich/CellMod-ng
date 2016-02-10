package com.jordanbussanich.cellmodng.commands;

import com.jordanbussanich.cellmodng.Carrier;
import com.jordanbussanich.cellmodng.CellMod;
import com.jordanbussanich.cellmodng.TLogger;

import java.sql.ResultSet;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CarrierCmd implements CommandExecutor {
    private CellMod plugin;

    public CarrierCmd(CellMod p) {
        plugin = p;
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
        if (plugin.hasPermission(p, "cellmod.use")) {
            if (strings.length < 1) {
                // /carrier
                if (plugin.getPlayerCarrier(p) != null) {
                    p.sendMessage(plugin.languageConfig.getString("CarrierIs") + ChatColor.GREEN + plugin.getPlayerCarrier(p).getName());
                    ResultSet rs = plugin.getResult("SELECT * FROM players WHERE Player='" + p.getName() + "';");
                    String number = null;
                    String count = null;
                    try {
                        while (rs.next()) {
                            number = rs.getString("Number");
                            count = rs.getString("Plan");
                        }
                    } catch (Exception ex) {
                        TLogger.log(Level.SEVERE, "Failed to read player number!");
                    }
                    p.sendMessage(plugin.languageConfig.getString("NumberIs") + ChatColor.GREEN + number);
                    p.sendMessage(plugin.languageConfig.getString("MessagesCost") + ChatColor.RED + plugin.getPlayerCarrier(p).getCost().toString());
                } else {
                    cs.sendMessage(ChatColor.RED + plugin.languageConfig.getString("JoinNetwork"));
                }
            } else {
                //carrier []
                if (strings[0].equals("create")) {
                    if (strings[1] != null) {
                        if (plugin.hasPermission(p, "cellmod.createcarrier")) {
                            plugin.runInsertQuery("INSERT INTO carriers (Name, Owner, MessagePrice) VALUES ('" + strings[1] + "', '" + p.getName() + "', '1.0');");
                            plugin.carriers.put(strings[1], new Carrier(plugin, strings[1], p.getName(), 1.0));
                            plugin.runUpdateQuery("UPDATE players SET Carrier='" + strings[1] + "' WHERE Player='" + p.getName() + "';");
                            p.sendMessage(ChatColor.GREEN + plugin.languageConfig.getString("CreateCarrier"));
                        } else {
                            p.sendMessage(ChatColor.RED + plugin.languageConfig.getString("NoPermission"));
                        }
                    } else {
                        p.sendMessage(ChatColor.RED + "/carrier create [name]");
                    }
                } else {
                    if (strings[0].equals("setprice")) {
                        if (strings[1] != null) {
                            Carrier playercarrier = plugin.getPlayerCarrier(p);
                            if (plugin.isPlayerCarrierOwner(p, playercarrier)) {
                                plugin.runUpdateQuery("UPDATE carriers SET MessagePrice='" + strings[1] + "' WHERE Name='" + playercarrier.getName() + "';");
                                plugin.carriers.remove(playercarrier);
                                playercarrier.setCost(Double.valueOf(strings[1]));
                                plugin.carriers.put(playercarrier.getName(), playercarrier);
                                p.sendMessage(ChatColor.GREEN + plugin.languageConfig.getString("ChangedPrice"));
                            } else {
                                // no permission
                                p.sendMessage(ChatColor.RED + plugin.languageConfig.getString("NoPermission"));
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "/carrier setprice [price]");
                        }
                    } else {
                        if (strings[0].equals("join")) {
                            if (strings[1] != null) {
                                if (plugin.carriers.get(strings[1]) != null) {
                                    plugin.runUpdateQuery("UPDATE players SET Carrier='" + strings[1] + "', Plan='0' WHERE Player='" + p.getName() + "';");
                                    p.sendMessage(ChatColor.GREEN + plugin.languageConfig.getString("JoinedCarrier"));
                                } else {
                                    p.sendMessage(ChatColor.RED + plugin.languageConfig.getString("InvaildCarrier"));
                                }
                            } else {
                                p.sendMessage(ChatColor.RED + "/carrier join [name]");
                            }
                        } else {
                            p.sendMessage(ChatColor.RED + "/carrier [create/setprice/join]");
                        }
                    }
                }
            }
        }
        return true;
    }
}