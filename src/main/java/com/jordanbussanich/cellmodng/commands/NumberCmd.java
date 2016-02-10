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

public class NumberCmd implements CommandExecutor {

    private final CellMod plugin;
    private CommandSender cSender;

    public NumberCmd(CellMod instance) {
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
        Player player = (Player) cs;
        if (plugin.hasPermission(player, "cellmod.changenumber")) {
            if (strings.length > 0) {
                ResultSet rs = plugin.getResult("SELECT * FROM players WHERE Player='" + player.getName() + "' AND Changed='true';");
                Boolean changed = false;
                try {
                    while (rs.next()) {
                        changed = true;
                    }
                } catch (SQLException ex) {
                    TLogger.log(Level.SEVERE, "Failed to read player number for API access!");
                }
                rs = plugin.getResult("SELECT * FROM players WHERE Number='" + strings[0] + "';");
                Boolean inuse = false;
                try {
                    while (rs.next()) {
                        inuse = true;
                    }
                } catch (SQLException ex) {
                    TLogger.log(Level.SEVERE, "Failed to read player number for API access!");
                }
                if (strings[0].equals(plugin.mainConfig.getString("phone.specialnumbers.carrier")) || strings[0].equals(plugin.mainConfig.getString("phone.specialnumbers.emergency"))) {
                    inuse = true;
                }
                if (changed == false) {
                    if (inuse == false) {
                        plugin.runUpdateQuery("UPDATE players SET Number='" + strings[0] + "', Changed='true' WHERE Player='" + player.getName() + "';");
                        player.sendMessage(ChatColor.GREEN + plugin.languageConfig.getString("NumChanged"));
                    } else {
                        player.sendMessage(ChatColor.RED + plugin.languageConfig.getString("NumAlready"));
                    }
                } else {
                    player.sendMessage(ChatColor.RED + plugin.languageConfig.getString("NumNoMore"));
                }
            } else {
                player.sendMessage(ChatColor.RED + plugin.languageConfig.getString("NumWrongLength"));
            }
            return true;
        } else {
            player.sendMessage(ChatColor.RED + plugin.languageConfig.getString("NoPermission"));
            return true;
        }
    }
}

