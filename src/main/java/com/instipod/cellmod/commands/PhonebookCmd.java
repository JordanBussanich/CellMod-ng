package com.instipod.cellmod.commands;

// This file written by Michael (Instipod)

import com.instipod.cellmod.CellMod;
import com.instipod.cellmod.TLogger;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PhonebookCmd implements CommandExecutor {
    private CellMod plugin;
    
    public PhonebookCmd(CellMod p) {
        plugin = p;
    }
    
    @Override
    public boolean onCommand(CommandSender cs, Command cmnd, String string, String[] strings) {
        Player pl = null;
        try {
            pl = (Player) cs;
        } catch (Exception ex) {
            cs.sendMessage("Only players may use this command.");
            return true;
        }
        Player player = (Player) cs;
        if (plugin.hasPermission(player, "cellmod.use")) {
            player.sendMessage(plugin.lang.getProperty("PhonebookTitle") + ":");
            for (Player p : plugin.getServer().getOnlinePlayers()) {
                 ResultSet rs = plugin.getResult("SELECT * FROM players WHERE Player='" + p.getName() + "';");
        String number = null;
        try {
            while (rs.next()) {
                number = rs.getString("Number");
            }
        } catch (SQLException ex) {
            TLogger.log(Level.SEVERE, "Failed to read player number for phonebook!");
        }
                player.sendMessage(p.getName() + " - " + number);
            }
        } else {
            player.sendMessage(ChatColor.RED + plugin.lang.getProperty("NoPermission"));
        }
        return true;
    }
    
}
