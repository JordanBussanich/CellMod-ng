package com.jordanbussanich.cellmodng.commands;

import com.jordanbussanich.cellmodng.CellMod;
import com.jordanbussanich.cellmodng.TLogger;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CellCmd implements CommandExecutor {

    private final CellMod plugin;
    private CommandSender cSender;

    public CellCmd(CellMod instance) {
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
        if (strings.length > 0) {
            if ("about".equals(strings[0])) {
                cs.sendMessage("This server is running version " + plugin.getDescription().getVersion() + " of the CellMod plugin by Instipod.");
            }
            if ("debug".equals(strings[0])) {
                cs.sendMessage(plugin.languageConfig.getString("Header"));
                cs.sendMessage(plugin.getPlayerCarrier((Player) cs).getName() + " " + plugin.getSignal((Player) cs, 0.0));
                cs.sendMessage("Distance: " + plugin.getDistance(p, 0.0).toString());
            }
        } else {
            if (plugin.hasPermission((Player) cs, "cellmod.use")) {
                if (plugin.getPlayerCarrier((Player) cs) != null) {
                    cs.sendMessage(plugin.languageConfig.getString("Header"));
                    cs.sendMessage(plugin.getPlayerCarrier((Player) cs).getName() + " " + plugin.getSignal((Player) cs, 0.0));
                    Player player = (Player) cs;
                    ResultSet rs = plugin.getResult("SELECT * FROM players WHERE Player='" + player.getName() + "';");
                    String number = null;
                    try {
                        while (rs.next()) {
                            number = rs.getString("Number");
                        }
                    } catch (SQLException ex) {
                        TLogger.log(Level.SEVERE, "Failed to read player number!");
                    }
                    player.sendMessage(plugin.languageConfig.getString("NumberIs") + " " + number);
                    cs.sendMessage(plugin.languageConfig.getString("TypeSend"));
                } else {
                    cs.sendMessage(ChatColor.RED + plugin.languageConfig.getString("JoinNetwork"));
                }
            } else {
                cs.sendMessage(ChatColor.RED + plugin.languageConfig.getString("NoPermission"));
            }

        }
        return true;
    }
}
